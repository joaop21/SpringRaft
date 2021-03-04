package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.MessageSubscriber;
import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")
public class PeerWorker implements Runnable, MessageSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(PeerWorker.class);

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


    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            InetSocketAddress targetServer
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServer.getHostName() + ":" + targetServer.getPort();
        this.lock = new ReentrantLock();
        this.newMessages = lock.newCondition();
        this.remainingMessages = 0;
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {
        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        while (true) {

            this.waitForNewMessages();

            // get message to send
            Message message = this.consensusModule.getNextMessage(this.targetServerName);

            // send message
            if (message != null)
                this.send(message);

        }

    }

    /**
     * Method that waits for new messages.
     * */
    private void waitForNewMessages() {
        lock.lock();
        try {

            while(this.remainingMessages == 0)
                this.newMessages.await();

            this.remainingMessages--;

        } catch (InterruptedException e) {
            log.error("Break Await", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Depending on the message class, it decides which method to invoke.
     *
     * @param message Message to send.
     * */
    private void send(Message message) {

        if(message instanceof RequestVote) {

            RequestVoteReply reply;
            do {
                reply = this.requestVote((RequestVote) message);
            } while (reply == null && this.remainingMessages == 0);

            if (reply != null)
                this.consensusModule.requestVoteReply(reply);

        } else if (message instanceof AppendEntries) {

            AppendEntries appendEntries = (AppendEntries) message;
            AppendEntriesReply reply;

            // if it is an heartbeat
            if (appendEntries.getEntries().size() == 0) {

                do {

                    long start = System.currentTimeMillis();

                    reply = this.appendEntries(appendEntries);

                    if (reply != null) {

                        this.consensusModule.appendEntriesReply(reply);

                        // sleep for the remaining time, if any
                        this.waitOnConditionForAnAmountOfTime(start);

                    }


                } while (this.remainingMessages == 0);

            } else {

                // when there are entries to replicate
                // not needed to leader election

            }

        }

    }

    /**
     * TODO
     * */
    private RequestVoteReply requestVote(RequestVote requestVote) {

        long start = System.currentTimeMillis();

        try {

            RequestVoteReply reply = this.outbound.requestVote(this.targetServerName, requestVote);
            log.info(reply.toString());

            return reply;

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            this.waitOnConditionForAnAmountOfTime(start);

        } catch (TimeoutException e) {

            // If the request vote communication exceeded heartbeat timout
            log.warn("Communication to server " + this.targetServerName + " exceeded heartbeat timeout!!");

        } catch (Exception e) {

            // If another exception occurs
            log.error("EXCEPTION NOT EXPECTED", e);

        }

        return null;

    }

    private AppendEntriesReply appendEntries (AppendEntries appendEntries) {

        long start = System.currentTimeMillis();

        try {

            AppendEntriesReply reply = this.outbound.appendEntries(this.targetServerName, appendEntries);
            log.info(reply.toString());

            return reply;

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            this.waitOnConditionForAnAmountOfTime(start);

        } catch (TimeoutException e) {

            // If the request vote communication exceeded heartbeat timout
            log.warn("Communication to server " + this.targetServerName + " exceeded heartbeat timeout!!");

        } catch (Exception e) {

            // If another exception occurs
            log.error("EXCEPTION NOT EXPECTED", e);

        }

        return null;

    }

    /**
     * Method thar makes a thread wait on a conditional variable, until something signals the condition
     * or an amount of time passes without anything signals the condition.
     *
     * @param startTime Time in milliseconds used to calculate the remaining time
     *                  until the thread has to continue executing.
     * */
    private void waitOnConditionForAnAmountOfTime(long startTime) {

        long remaining = this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - startTime);

        if (remaining > 0) {

            lock.lock();
            try {

                if(this.remainingMessages == 0)
                    this.newMessages.await(remaining, TimeUnit.MILLISECONDS);

            } catch (InterruptedException exception) {

                log.error("Exception while awaiting", exception);

            } finally {
                lock.unlock();
            }

        }

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Override
    public void newMessage() {

        lock.lock();
        try {
            this.remainingMessages++;
            this.newMessages.signal();
        } finally {
            lock.unlock();
        }

    }
}
