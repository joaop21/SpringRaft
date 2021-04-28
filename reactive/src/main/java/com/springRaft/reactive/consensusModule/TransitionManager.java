package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.worker.StateTransition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.OptionalLong;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class TransitionManager {

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Scheduler for scheduled tasks */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    public TransitionManager(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            @Qualifier(value = "transitionTaskScheduler") Scheduler scheduler
    ) {
        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.scheduler = scheduler;
    }

    /* --------------------------------------------------- */

    /**
     * Creates a scheduled timeout, based on raft properties.
     *
     * @return Mono<Disposable> Mono with a disposable task, so it can be disposed later.
     * */
    public Mono<Disposable> setElectionTimeout() {

        Mono<Long> timeoutMono = this.getRandomLongBetweenRange(
                this.raftProperties.getElectionTimeoutMin().toMillis(),
                this.raftProperties.getElectionTimeoutMax().toMillis()
        );

        Mono<StateTransition> transitionMono = Mono.defer(() ->
                Mono.just(this.applicationContext.getBean(StateTransition.class, applicationContext, consensusModule, Candidate.class))
        );

        return Mono.zip(timeoutMono, transitionMono)
                .flatMap(tuple ->
                        Mono.defer(() -> Mono.just(
                                this.scheduler.schedule(tuple.getT2(), tuple.getT1(), TimeUnit.MILLISECONDS)
                        ))
                );

    }

    /**
     * Method for creating a new follower state transition which takes place on transition scheduler.
     * */
    public void setNewFollowerState() {

        Mono.defer(() -> Mono.just(
                applicationContext
                        .getBean(StateTransition.class, applicationContext, consensusModule, Follower.class))
        )
                .doOnNext(this.scheduler::schedule)
                .subscribe();

    }

    /* --------------------------------------------------- */

    /**
     * Calculates a random long between a minimum and a maximum.
     *
     * @param min Minimum long in the range.
     * @param max Maximum long in the range.
     *
     * @return Mono<Long> Random calculated Long.
     * */
    private Mono<Long> getRandomLongBetweenRange(long min, long max){

        return Mono.defer(() -> {
            OptionalLong op = new Random().longs(min, max+1).findFirst();
            return Mono.just(op.isPresent() ? op.getAsLong() : min);
        });
    }

}
