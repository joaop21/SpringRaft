package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Scope("singleton")
public class ConsensusModule implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(ConsensusModule.class);

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* Sink for publish operations */
    private final Sinks.Many<Mono<?>> operationPublisher;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public ConsensusModule() {
        this.current = null;
        this.operationPublisher = Sinks.many().unicast().onBackpressureBuffer();

        // subscribe to Concurrency Control Pipeline
        this.concurrencyControlPipeline().subscribe();
    }

    /* --------------------------------------------------- */

    /**
     * Setter method which sets the new state in consensus module.
     *
     * @param state The new raft state of this consensus module.
     * */
    public void setCurrentState(RaftState state) {
        this.current = state;
    }

    /**
     * Setter method which sets the new state in consensus module and starts that state.
     *
     * @param state The new raft state of this consensus module.
     * */
    public Mono<Void> setAndStartNewState(RaftState state) {
        return publishOperation(
                Mono.<Mono<Void>>create(monoSink -> {
                    this.setCurrentState(state);
                    monoSink.success(this.start());
                })
                .flatMap(mono -> mono)
        ).cast(Void.class);
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return publishAndSubscribeOperation(
                Mono.defer(() -> this.current.appendEntries(appendEntries))
        ).cast(AppendEntriesReply.class);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return publishOperation(
                Mono.defer(() -> this.current.appendEntriesReply(appendEntriesReply,from))
        ).cast(Void.class);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return publishAndSubscribeOperation(
                Mono.defer(() -> this.current.requestVote(requestVote))
        ).cast(RequestVoteReply.class);
    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return publishOperation(
                Mono.defer(() -> this.current.requestVoteReply(requestVoteReply))
        ).cast(Void.class);
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return (Mono<Pair<Message, Boolean>>) publishAndSubscribeOperation(
                Mono.defer(() -> this.current.getNextMessage(to)));
    }

    @Override
    public Mono<Void> start() {
        return this.current.start();
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private Flux<?> concurrencyControlPipeline() {
        return this.operationPublisher.asFlux().flatMap(mono -> mono, 1);
    }

    /* --------------------------------------------------- */

    /**
     * Method that publishes the mono operation into the publisher and waits for the response in a specific sink.
     *
     * @param operation Mono operation to invoke in the current state.
     *
     * @return Response of the invocation of the operation in the current state.
     * */
    private Mono<?> publishAndSubscribeOperation(Mono<?> operation) {
        return Mono.just(Sinks.one())
                .doOnNext(responseSink -> {
                    while (this.operationPublisher.tryEmitNext(operation.doOnSuccess(responseSink::tryEmitValue)) != Sinks.EmitResult.OK);
                })
                .flatMap(Sinks.Empty::asMono);
    }

    /**
     * Method that publishes the mono operation into the publisher and doesn't wait for the response.
     *
     * @param operation Mono operation to invoke in the current state.
     * */
    private Mono<Void> publishOperation(Mono<?> operation) {
        return Mono.create(monoSink -> {
            while (this.operationPublisher.tryEmitNext(operation) != Sinks.EmitResult.OK);
            monoSink.success();
        });
    }

}
