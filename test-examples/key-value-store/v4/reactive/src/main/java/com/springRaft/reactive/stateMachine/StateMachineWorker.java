package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.persistence.log.Entry;
import com.springRaft.reactive.persistence.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Comparator;

@Component
@Scope("singleton")
public class StateMachineWorker implements CommitmentSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Strategy for applying commands to the State Machine */
    private StateMachineStrategy strategy;

    /* Service to access persisted log repository */
    private final LogService logService;

    /* Map that contains the clients waiting requests */
    protected final WaitingRequests waitingRequests;

    /* Sink for publish new commits */
    private final Sinks.Many<Boolean> commitSink;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public StateMachineWorker(LogService logService, WaitingRequests waitingRequests) {
        this.strategy = null;
        this.logService = logService;
        this.waitingRequests = waitingRequests;
        this.commitSink = Sinks.many().unicast().onBackpressureBuffer();

        this.commitmentsHandler().subscribe();
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public void setStrategy(StateMachineStrategy stateMachineStrategy) {
        this.strategy = stateMachineStrategy;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> newCommit() {
        return Mono.just(this.commitSink.tryEmitNext(true)).then();
    }

    /**
     * TODO
     * */
    private Flux<?> commitmentsHandler() {
        return this.commitSink.asFlux().flatMap(bool -> this.applyCommitsToStateMachine(), 1);
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private Mono<Void> applyCommitsToStateMachine() {

        return this.logService.getState()
                .flatMapMany(logState -> {

                    long indexToStart = logState.getLastApplied() + 1;
                    long lastCommitted = logState.getCommittedIndex() + 1;

                    return this.logService.getEntriesBetweenIndexes(indexToStart, lastCommitted);
                })
                .collectSortedList(Comparator.comparing(Entry::getIndex))
                .flatMapIterable(entries -> entries)
                // apply command depending on the strategy
                .flatMap(entry ->
                        this.strategy.apply(entry.getCommand())
                                // increment lastApplied in the Log State
                                .flatMap(response -> this.logService.incrementLastApplied().map(logState -> response))
                                // notify client of the response
                                .flatMap(response -> this.waitingRequests.putResponse(entry.getIndex(), response))
                ,1)
                .then();

    }

}
