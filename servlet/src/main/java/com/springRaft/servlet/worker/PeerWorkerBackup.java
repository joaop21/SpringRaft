package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.MessageSubscriber;
import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import com.springRaft.servlet.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;


@Component
@Scope("prototype")
public class PeerWorkerBackup implements Runnable, MessageSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(PeerWorkerBackup.class);

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* String of the address of the target server */
    private final String targetServerName;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for state to changes */
    private final Condition newMessages;

    /* Remaining messages to send */
    private Integer remainingMessages;

    /* Boolean that flags if PeerWorker should work */
    private Boolean active;


    /* --------------------------------------------------- */

    public PeerWorkerBackup(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            String targetServerName
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServerName;
        this.lock = new ReentrantLock();
        this.newMessages = lock.newCondition();
        this.remainingMessages = 0;
        this.active = false;
    }

    /* --------------------------------------------------- */

    public void newMessage() {

        lock.lock();
        try {
            this.active = true;
            this.remainingMessages++;
            this.newMessages.signal();
        } finally {
            lock.unlock();
        }

    }

    public void clearMessages() {

        lock.lock();
        try {
            this.active = false;
            this.remainingMessages = 0;
            this.newMessages.signal();
        } finally {
            lock.unlock();
        }

    }

    /* --------------------------------------------------- */

    @Override
    public void run() {
        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        while (true) {

            this.waitForNewMessages();

            // get message to send
            Pair<Message,Boolean> pair = this.consensusModule.getNextMessage(this.targetServerName);

            // send message
            if (pair.first() != null)
                this.send(pair.first(), pair.second());

        }

    }

    /**
     * Method that waits for new messages.
     * */
    private void waitForNewMessages() {

        lock.lock();
        try {

            while (!this.active)
                this.newMessages.await();

            if (this.remainingMessages > 0)
                this.remainingMessages--;

        } catch (InterruptedException interruptedException) {

            log.error("Break Await in waitForNewMessages");

        } finally {
            lock.unlock();
        }

    }

    /**
     * Depending on the message class, it decides which method to invoke.
     *
     * @param message Message to send.
     * @param heartbeat Boolean that signals if a message is an heartbeat.
     * */
    private void send(Message message, Boolean heartbeat) {

        if(message instanceof RequestVote) {

            this.handleRequestVote((RequestVote) message);

        } else if (message instanceof AppendEntries) {

            if (heartbeat) {
                // if it is an heartbeat

                this.handleHeartbeat((AppendEntries) message);

            } else {
                // if it has an Entry to add to the log or it is an AppendEntries to find a match index

                this.handleNormalAppendEntries((AppendEntries) message);

            }

        }

    }

    /**
     * TODO
     * */
    private void handleRequestVote(RequestVote requestVote) {

        RequestVoteReply reply;

        do {

            reply = (RequestVoteReply) this.sendRPCHandler(() -> this.outbound.requestVote(this.targetServerName, requestVote));

        } while (reply == null && this.active && this.remainingMessages == 0);

        if (reply != null && this.active) {
            this.clearMessages();
            this.consensusModule.requestVoteReply(reply);
        }

    }

    /**
     * TODO
     * */
    private void handleHeartbeat(AppendEntries appendEntries) {

        AppendEntriesReply reply;

        do {

            long start = System.currentTimeMillis();

            reply = (AppendEntriesReply) this.sendRPCHandler(() -> this.outbound.appendEntries(this.targetServerName, appendEntries));

            if (reply != null && this.active) {

                this.consensusModule.appendEntriesReply(reply, this.targetServerName);

                if (!reply.getSuccess())
                    break;

                // sleep for the remaining time, if any
                this.waitWhileCondition(start, () -> this.active && this.remainingMessages == 0);

                // go get the next heartbeat because the committed index may have changed
                break;

            }


        } while (this.active && this.remainingMessages == 0);

    }

    /**
     * TODO
     * */
    private void handleNormalAppendEntries(AppendEntries appendEntries) {

        AppendEntriesReply reply;

        do {

            reply = (AppendEntriesReply) this.sendRPCHandler(() -> this.outbound.appendEntries(this.targetServerName, appendEntries));

            if (reply != null && this.active) {

                this.consensusModule.appendEntriesReply(reply, this.targetServerName);
                break;

            }

            // if you get here, it means that the reply is null
        } while (this.active);

    }

    /**
     * TODO
     * */
    private Message sendRPCHandler(Callable<? extends Message> rpc) {

        long start = System.currentTimeMillis();

        try {

            return rpc.call();

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            this.waitWhileCondition(start, () -> this.active);

        } catch (TimeoutException e) {

            // If the communication exceeded heartbeat timeout
            // log.warn("Communication to server " + this.targetServerName + " exceeded heartbeat timeout!!");

        } catch (Exception e) {

            // If another exception occurs
            log.error("Exception not expected in sendRPCHandler method");

        }

        return null;

    }

    /**
     * Method that makes a thread wait on a conditional variable, until something signals the condition
     * or an amount of time passes without anything signals the condition.
     *
     * @param startTime Time in milliseconds used to calculate the remaining time
     *                  until the thread has to continue executing.
     * @param condition Condition to evaluate. If true, thread awaits on condition.
     * */
    private void waitWhileCondition(long startTime, BooleanSupplier condition) {

        long remaining = this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - startTime);

        if (remaining > 0) {

            lock.lock();
            try {

                if(condition.getAsBoolean())
                    this.newMessages.await(remaining, TimeUnit.MILLISECONDS);

            } catch (InterruptedException exception) {

                log.error("Exception while awaiting on waitOnConditionForAnAmountOfTime method");

            } finally {
                lock.unlock();
            }

        }

    }





    /* --------------------------------------------------- */







    @Override
    public void sendRequestVote(RequestVote requestVote) {

    }

    @Override
    public void sendAuthorityHeartbeat(AppendEntries heartbeat) {

    }

    @Override
    public void sendHeartbeat(AppendEntries heartbeat, String to) {

    }

    @Override
    public void sendAppendEntries(AppendEntries appendEntries, String to) {

    }

    @Override
    public void newClientRequest() {

    }

    @Override
    public void newFollowerState() {

    }
}
