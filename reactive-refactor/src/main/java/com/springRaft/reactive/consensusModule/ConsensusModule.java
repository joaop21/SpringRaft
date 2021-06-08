package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.util.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@Scope("singleton")
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    private final Sinks.Many<Mono<?>> operationPublisher;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public ConsensusModule() {
        this.current = null;
        this.operationPublisher = Sinks.many().multicast().onBackpressureBuffer();

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
        return publishAndSubscribeOperation(
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
        return publishAndSubscribeOperation(this.current.appendEntries(appendEntries)).cast(AppendEntriesReply.class);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return publishAndSubscribeOperation(this.current.appendEntriesReply(appendEntriesReply,from)).cast(Void.class);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return publishAndSubscribeOperation(this.current.requestVote(requestVote)).cast(RequestVoteReply.class);
    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return publishAndSubscribeOperation(this.current.requestVoteReply(requestVoteReply)).cast(Void.class);
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return (Mono<Pair<Message, Boolean>>) publishAndSubscribeOperation(this.current.getNextMessage(to));
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
     * TODO
     * */
    private Mono<?> publishAndSubscribeOperation(Mono<?> operation) {
        return Mono.just(Sinks.one())
                .doOnNext(responseSink ->
                    this.operationPublisher.tryEmitNext(operation.doOnSuccess(responseSink::tryEmitValue))
                )
                .flatMap(Sinks.Empty::asMono);
    }

}
