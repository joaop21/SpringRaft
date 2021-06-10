package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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

    /* Remaining clientRequests to send */
    private final AtomicInteger clientRequests;

    /* Sink for publish new rpcs */
    private final Sinks.Many<Mono<?>> rpcSink;

    /* Disposable of the ongoing communication */
    private Disposable ongoingCommunication;

    /* Timestamp in Milliseconds that marks the beginning of the last communication */
    private final AtomicLong communicationStart;

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

        this.clientRequests = new AtomicInteger(0);

        this.rpcSink = Sinks.many().unicast().onBackpressureBuffer();

        this.ongoingCommunication = null;
        this.communicationStart = new AtomicLong(0);

        this.communicationsHandler().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> sendRequestVote(RequestVote requestVote) {
        return Mono.just(this.rpcSink.tryEmitNext(this.handleRequestVote(requestVote))).then();
    }

    @Override
    public Mono<Void> sendAuthorityHeartbeat(AppendEntries heartbeat) {
        return Mono.just(this.rpcSink.tryEmitNext(this.handleHeartbeat(heartbeat))).then();
    }

    @Override
    public Mono<Void> sendHeartbeat(AppendEntries heartbeat, String to) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> sendAppendEntries(AppendEntries appendEntries, String to) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> newClientRequest() {
        return Mono.just(this.clientRequests.incrementAndGet()).then();
    }

    @Override
    public Mono<Void> newFollowerState() {
        return Mono.just(this.rpcSink.tryEmitNext(Mono.empty())).then();
    }

    /**
     * TODO
     * */
    private Flux<?> communicationsHandler() {
        return this.rpcSink.asFlux()
                .doOnNext(mono -> this.disposeCurrentAndSetNew(mono.subscribe()));
    }

    /**
     * Method that dispose the current communication task, if exists one, and set a new one.
     *
     * @param disposable New communication task
     * */
    private void disposeCurrentAndSetNew(Disposable disposable) {

        if (this.ongoingCommunication != null && !this.ongoingCommunication.isDisposed())
            this.ongoingCommunication.dispose();

        this.ongoingCommunication = disposable;

    }

    /* --------------------------------------------------- */

    /**
     * Method that handles the requestVoteRPC communication.
     *
     * @param requestVote Message to send to the target server.
     * */
    private Mono<Void> handleRequestVote(RequestVote requestVote) {

        AtomicReference<RequestVoteReply> reply = new AtomicReference<>(null);

        return this.sendRPCHandler(this.outbound.requestVote(this.targetServerName, requestVote))
                    .cast(RequestVoteReply.class)
                    //.doOnSuccess(result ->
                      //      log.info("\n\n" + this.targetServerName + ":\n" +
                        //            "REQUEST VOTE: " + requestVote +
                          //          "\nREQUEST VOTE REPLY: " + result + "\n")
                    //)
                    .doOnNext(reply::set)
                .repeat(() -> reply.get() == null)
                .next()
                .flatMap(this.consensusModule::requestVoteReply);

    }

    /**
     * Method that handles the heartbeats communication.
     *
     * @param appendEntries Message to send to the target server.
     * */
    private Mono<Void> handleHeartbeat(AppendEntries appendEntries) {

        AtomicReference<AppendEntriesReply> reply = new AtomicReference<>(null);

        return Mono.delay(Duration.ofMillis(
                this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - this.communicationStart.get())
        ))
                    .doOnNext(number -> this.communicationStart.set(System.currentTimeMillis()))
                    .flatMap(number -> this.sendRPCHandler(this.outbound.appendEntries(this.targetServerName, appendEntries)))
                    .cast(AppendEntriesReply.class)
                    .doOnSuccess(result ->
                          log.info("\n\n" + this.targetServerName + ":\n" +
                                "HEARTBEAT: " + appendEntries +
                                "\nHEARTBEAT REPLY: " + result + "\n")
                    )
                    .doFirst(() -> this.communicationStart.set(System.currentTimeMillis()))
                    .doOnNext(reply::set)
                    .filter(appendEntriesReply -> reply.get() != null)
                        .flatMap(appendEntriesReply -> this.consensusModule.appendEntriesReply(reply.get(), this.targetServerName))
                .repeat(() -> reply.get() == null)
                .next();

    }

    /**
     * Method that handles the communication of an RPC:
     *      1- Sends the RPC and waits for the response;
     *      2- If:
     *          a) The response is fine, just retrieve its value;
     *          b) If the target server is not alive, it waits at most the heartbeat time to retrieve no response;
     *          c) If the server didn't respond within the heartbeat time, no response is retrieved;
     *
     * @param rpcMono The Mono that represents the RPC to invoke.
     *
     * @return Response Message.
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
                            return Mono.delay(Duration.ofMillis(
                                    this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - start.get())
                            ))
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

}
