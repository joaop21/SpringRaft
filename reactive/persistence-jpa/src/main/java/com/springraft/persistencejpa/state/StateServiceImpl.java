package com.springraft.persistencejpa.state;

import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@Scope("singleton")
@Transactional
public class StateServiceImpl implements StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateServiceImpl.class);

    /* Repository for State operations */
    private final StateRepository repository;

    /* Raft properties that need to be accessed */
    //protected final RaftProperties raftProperties;

    /* Scheduler to execute database operations */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    public StateServiceImpl(
            StateRepository repository,
            //RaftProperties properties,
            @Qualifier("jpaScheduler") Scheduler jpaScheduler)
    {
        this.repository = repository;
        //this.raftProperties = properties;
        this.scheduler = jpaScheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<? extends State> getState() {
        return Mono.fromCallable(() -> this.repository.findById((long) 1))
                .subscribeOn(this.scheduler)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }

    @Override
    public Mono<? extends State> saveState(State state) {
        return Mono.fromCallable(() -> this.repository.save((StateImpl) state))
                .subscribeOn(this.scheduler)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<State> newCandidateState() {
        return this.getState()
                .cast(StateImpl.class)
                .flatMap(state -> {
                    state.setCurrentTerm(state.getCurrentTerm() + 1);
                    //state.setVotedFor(this.raftProperties.getHost());
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<Long> getCurrentTerm() {
        return this.getState()
                .cast(StateImpl.class)
                .map(StateImpl::getCurrentTerm);
    }

    @Override
    public Mono<String> getVotedFor() {
        return this.getState()
                .cast(StateImpl.class)
                .map(state ->
                        state.getVotedFor() == null
                                ? ""
                                : state.getVotedFor()
                );
    }

    @Override
    public Mono<State> setVotedFor(String votedFor) {
        return this.getState()
                .cast(StateImpl.class)
                .flatMap(state -> {
                    state.setVotedFor(votedFor);
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<State> setState(Long term, String votedFor) {
        return this.getState()
                .cast(StateImpl.class)
                .flatMap(state -> {
                    state.setCurrentTerm(term);
                    state.setVotedFor(votedFor);
                    return this.saveState(state);
                });
    }
}

