package com.springRaft.reactive.worker;

import com.springRaft.reactive.communication.message.*;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
    public Mono<Void> newMessage() {
        return Mono.defer(() -> {
                    lock.lock();
                    this.active = true;
                    this.remainingMessages++;
                    this.newMessages.signal();
                    lock.unlock();
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> clearMessages() {
        return Mono.defer(() -> {
                    lock.lock();
                    this.active = false;
                    this.remainingMessages = 0;
                    this.newMessages.signal();
                    lock.unlock();
                    return Mono.empty();
                });
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        // wait for messages
        this.waitForNewMessages()
                // get next message
                .flatMap(hasMessage -> this.consensusModule.getNextMessage(this.targetServerName))
                .filter(pair -> pair.getFirst() != null)
                    //.doOnNext(pair -> log.info(pair.getFirst().toString() + " for " + this.targetServerName))
                    // send message
                    .flatMap(pair -> this.send(pair.getFirst(), pair.getSecond()))
                .repeat()
                .blockLast();


    }

    /**
     * Method that waits for new messages.
     *
     * @return Boolean which represents the operation success.
     * */
    private Mono<Boolean> waitForNewMessages() {

        return Mono.create(monoSink -> {

            lock.lock();

            try {

                while (!this.active)
                    this.newMessages.await();

                if (this.remainingMessages > 0)
                    this.remainingMessages--;

                monoSink.success(true);

            } catch (InterruptedException interruptedException) {

                log.error("Break Await in waitForNewMessages");

                monoSink.error(interruptedException);

            } finally {
                lock.unlock();
            }

        });

    }

    /**
     * Depending on the message class, it decides which method to invoke.
     *
     * @param message Message to send.
     * @param heartbeat Boolean that signals if a message is an heartbeat.
     * */
    private Mono<Void> send(Message message, Boolean heartbeat) {

        return Mono.defer(() -> {

            if(message instanceof RequestVote) {

                return this.handleRequestVote((RequestVote) message);

            } else if (message instanceof AppendEntries) {

                if (heartbeat) {
                    // if it is an heartbeat

                    return this.handleHeartbeat((AppendEntries) message);

                } else {
                    // if it has an Entry to add to the log or it is an AppendEntries to find a match index

                    return this.handleNormalAppendEntries((AppendEntries) message);

                }

            }

            return Mono.empty();

        });

    }

    /**
     * Method that handles the requestVoteRPC communication.
     *
     * @param requestVote Message to send to the target server.
     * */
    private Mono<Void> handleRequestVote(RequestVote requestVote) {

        AtomicReference<RequestVoteReply> reply = new AtomicReference<>(null);

        return this.sendRPCHandler(this.outbound.requestVote(this.targetServerName, requestVote))
                    .cast(RequestVoteReply.class)
                    .doOnNext(reply::set)
                .repeat(() -> reply.get() == null && this.active && this.remainingMessages == 0)
                .next()
                .filter(requestVoteReply -> reply.get() != null && this.active)
                    .flatMap(requestVoteReply ->
                            this.clearMessages()
                                .then(this.consensusModule.requestVoteReply(reply.get()))
                    );

    }

    /**
     * Method that handles the heartbeats communication.
     *
     * @param appendEntries Message to send to the target server.
     * */
    private Mono<Void> handleHeartbeat(AppendEntries appendEntries) {

        AtomicReference<AppendEntriesReply> reply = new AtomicReference<>(null);
        AtomicLong start = new AtomicLong();

        return this.sendRPCHandler(this.outbound.appendEntries(this.targetServerName, appendEntries))
                .cast(AppendEntriesReply.class)
                .doFirst(() -> start.set(System.currentTimeMillis()))
                .doOnNext(reply::set)
                .filter(appendEntriesReply -> reply.get() != null && this.active)
                    .flatMap(appendEntriesReply ->
                            this.consensusModule.appendEntriesReply(reply.get(), this.targetServerName)
                                    .then(Mono.just(appendEntriesReply))
                    )
                    .filter(appendEntriesReply -> reply.get().getSuccess())
                        .flatMap(appendEntriesReply ->
                                this.waitWhileCondition(start.get(), () -> this.active && this.remainingMessages == 0)
                        )
                .repeat(() -> (this.active && this.remainingMessages == 0) && !(reply.get() != null && this.active))
                .next()
                .then();

    }

    /**
     * TODO
     * */
    private Mono<Void> handleNormalAppendEntries(AppendEntries appendEntries) {

        AtomicReference<AppendEntriesReply> reply = new AtomicReference<>(null);

        return this.sendRPCHandler(this.outbound.appendEntries(this.targetServerName, appendEntries))
                .cast(AppendEntriesReply.class)
                .doOnNext(reply::set)
                .filter(appendEntriesReply -> reply.get() != null && this.active)
                    .flatMap(appendEntriesReply -> this.consensusModule.appendEntriesReply(reply.get(), this.targetServerName))
                .repeat(() -> this.active && !(reply.get() != null && this.active))
                .next();

    }

    /**
     * TODO
     * */
    private Mono<? extends Message> sendRPCHandler(Mono<? extends Message> rpcMono) {

        AtomicLong start = new AtomicLong();

        return rpcMono
                .doFirst(() -> start.set(System.currentTimeMillis()))
                .onErrorResume(error -> {

                    if (error instanceof WebClientRequestException) {

                        if (((WebClientRequestException)error).contains(ConnectException.class)) {
                            // If target server is not alive

                            log.warn("Server " + this.targetServerName + " is not up!!");

                            // sleep for the remaining time, if any
                            return this.waitWhileCondition(start.get(), () -> this.active)
                                    .then(Mono.empty());

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
    private Mono<Boolean> waitWhileCondition(long startTime, BooleanSupplier condition) {

        return Mono.just(this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - startTime))
                .filter(remaining -> remaining > 0)
                .flatMap(remaining ->

                    Mono.<Boolean>create(monoSink -> {
                        lock.lock();
                        try {

                            if(condition.getAsBoolean())
                                this.newMessages.await(remaining, TimeUnit.MILLISECONDS);

                            monoSink.success(true);

                        } catch (InterruptedException exception) {

                            log.error("Exception while awaiting on waitOnConditionForAnAmountOfTime method");

                            monoSink.error(exception);

                        } finally {
                            lock.unlock();
                        }
                    })

                )
                .switchIfEmpty(Mono.just(false));

    }

}
