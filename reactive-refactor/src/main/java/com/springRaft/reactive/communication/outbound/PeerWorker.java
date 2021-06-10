package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

@Component
@Scope("prototype")
public class PeerWorker implements MessageSubscriber {

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

    /* Sink for publish events of messages */
    private final Sinks.Many<Mono<?>> messageSink;

    /* Sink for publish events of messages */
    private final Sinks.Many<Boolean> hasMessages;

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

        this.messageSink = Sinks.many().unicast().onBackpressureBuffer();
        this.hasMessages = Sinks.many().multicast().directBestEffort();
        this.remainingMessages = 0;
        this.active = false;

        this.run().subscribe();
        this.messageSubscriberHandler().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> newMessage() {

        return Mono.defer(() -> {

            Mono<Void> mono = Mono.defer(() -> {
                this.active = true;
                this.remainingMessages++;
                return Mono.empty();
            });

            this.messageSink.tryEmitNext(mono);

            return Mono.empty();
        });

    }

    @Override
    public Mono<Void> clearMessages() {

        return Mono.defer(() -> {

            Mono<Void> mono = Mono.defer(() -> {
                this.active = false;
                this.remainingMessages = 0;
                return Mono.empty();
            });

            this.messageSink.tryEmitNext(mono);

            return Mono.empty();
        });

    }

    /**
     * TODO
     * */
    private Flux<?> messageSubscriberHandler() {
        return this.messageSink.asFlux()
                .flatMap(mono ->
                        mono.doOnSuccess(result ->
                                this.hasMessages.tryEmitNext(this.active)
                        ), 1);
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private Flux<?> run() {

        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        // wait for messages
        return this.waitForNewMessages()
                // get next message
                .flatMap(hasMessage -> this.consensusModule.getNextMessage(this.targetServerName))
                .filter(pair -> pair.first() != null)
                    // send message
                    .flatMap(pair -> this.send(pair.first(), pair.second()))
                .repeat();

    }

    /**
     * Method that waits for new messages while this.active is false.
     *
     * @return Boolean which represents the operation success.
     * */
    private Mono<Boolean> waitForNewMessages() {

        // it has to be defer because this pipeline is invoked in a repeat operator
        // if it was only Mono.just(this.active), the pipeline would use always the value os this.active when
        // this pipeline was assembled
        return Mono.defer(() -> Mono.just(this.active))
                .flatMapMany(active -> active ? Flux.just(true) : this.hasMessages.asFlux())
                .flatMap(bool -> bool ? Mono.just(true) : Mono.empty(), 1)
                .next()
                .doOnNext(bool -> {
                    if (this.remainingMessages > 0)
                        this.remainingMessages--;
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
                    //return this.handleNormalAppendEntries((AppendEntries) message);
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
                    .doOnSuccess(result ->
                        log.info("\n\n" + this.targetServerName + ":\n" +
                                "REQUEST VOTE: " + requestVote +
                                "\nREQUEST VOTE REPLY: " + result + "\n")
                    )
                    .doOnNext(reply::set)
                .repeat(() -> reply.get() == null && this.active && this.remainingMessages == 0)
                .next()
                .filter(requestVoteReply -> reply.get() != null && this.active) // PROBLEM HERE
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
                    .doOnSuccess(result ->
                        log.info("\n\n" + this.targetServerName + ":\n" +
                                "APPEND ENTRIES: " + appendEntries +
                                "\nAPPEND ENTRIES REPLY: " + result + "\n")
                    )
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
     *
     * @return Boolean
     * */
    private Mono<Boolean> waitWhileCondition(long startTime, BooleanSupplier condition) {

        return Mono.just(this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - startTime))
                .filter(remaining -> remaining > 0)
                .flatMap(remaining -> {

                    if (condition.getAsBoolean())
                        return this.hasMessages.asFlux()
                                .next()
                                .timeout(Duration.ofMillis(remaining), Mono.just(true));

                    return Mono.just(false);

                });

    }
}
