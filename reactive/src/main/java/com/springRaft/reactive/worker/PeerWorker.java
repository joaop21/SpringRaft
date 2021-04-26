package com.springRaft.reactive.worker;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.communication.outbound.MessageSubscriber;
import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

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

    /* Boolean that flags if PeerWorker should work */
    private Boolean active;

    /* --------------------------------------------------- */

    public PeerWorker(
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

    @Override
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

    @Override
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

            // wait for messages
            this.waitForNewMessages();

            // get next message
            this.consensusModule.getNextMessage(this.targetServerName)
                    .doOnNext(pair -> {

                        // send message
                        if (pair.getFirst() != null)
                            this.send(pair.getFirst(), pair.getSecond());

                    })
                    .block();

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

        } /*else if (message instanceof AppendEntries) {

            if (heartbeat) {
                // if it is an heartbeat

                this.handleHeartbeat((AppendEntries) message);

            } else {
                // if it has an Entry to add to the log or it is an AppendEntries to find a match index

                this.handleNormalAppendEntries((AppendEntries) message);

            }

        }*/

    }

    /**
     * TODO
     * */
    private void handleRequestVote(RequestVote requestVote) {

        RequestVoteReply reply;

        do {

            reply = (RequestVoteReply) this.sendRPCHandler(this.outbound.requestVote(this.targetServerName, requestVote))
                    .block();

        } while (reply == null && this.active && this.remainingMessages == 0);

        if (reply != null && this.active) {
            //this.consensusModule.requestVoteReply(reply);
            this.clearMessages();
        }

    }

    /**
     * TODO
     * */
    private Mono<? extends Message> sendRPCHandler(Mono<? extends Message> rpcMono) {

        long start = System.currentTimeMillis();

        return rpcMono
                .onErrorResume(error -> {

                    if (error instanceof WebClientRequestException) {

                        if (((WebClientRequestException)error).contains(ConnectException.class)) {
                            // If target server is not alive

                            log.warn("Server " + this.targetServerName + " is not up!!");

                            // sleep for the remaining time, if any
                            this.waitWhileCondition(start, () -> this.active);

                        }

                    } else if (error instanceof TimeoutException) {

                        // If the communication exceeded heartbeat timeout

                    } else {

                        // If another exception occurs
                        log.error("Exception not expected in sendRPCHandler method\nError: " + error);

                    }

                    return Mono.empty();

                });

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

}
