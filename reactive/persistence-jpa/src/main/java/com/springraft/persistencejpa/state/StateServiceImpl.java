package com.springraft.persistencejpa.state;

import com.springraft.persistence.state.StateModel;
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
    public Mono<StateModel> getState() {
        return Mono.fromCallable(() -> this.repository.findById((long) 1))
                .subscribeOn(this.scheduler)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }

    @Override
    public Mono<? extends StateModel> saveState(Object state) {
        return Mono.fromCallable(() -> this.repository.save((com.springraft.persistencejpa.state.State)state))
                .subscribeOn(this.scheduler)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<StateModel> newCandidateState() {
        return this.getState()
                .flatMap(state -> {
                    state.setCurrentTerm(state.getCurrentTerm() + 1);
                    //state.setVotedFor(this.raftProperties.getHost());
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<Long> getCurrentTerm() {
        return this.getState()
                .map(StateModel::getCurrentTerm);
    }

    @Override
    public Mono<String> getVotedFor() {
        return this.getState()
                .map(state ->
                        state.getVotedFor() == null
                                ? ""
                                : state.getVotedFor()
                );
    }

    @Override
    public Mono<StateModel> setVotedFor(String votedFor) {
        return this.getState()
                .flatMap(state -> {
                    state.setVotedFor(votedFor);
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<StateModel> setState(Long term, String votedFor) {
        return this.getState()
                .flatMap(state -> {
                    state.setCurrentTerm(term);
                    state.setVotedFor(votedFor);
                    return this.saveState(state);
                });
    }
}

