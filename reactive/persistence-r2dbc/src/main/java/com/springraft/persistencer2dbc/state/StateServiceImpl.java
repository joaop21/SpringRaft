package com.springraft.persistencer2dbc.state;

import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@Transactional
@AllArgsConstructor
public class StateServiceImpl implements StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateServiceImpl.class);

    /* Repository for State operations */
    private final StateRepository repository;

    /* Raft properties that need to be accessed */
    //protected final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    @Override
    public Mono<State> getState() {
        return this.repository.findById((long) 1)
                .cast(State.class);
    }

    @Override
    public Mono<? extends State> saveState(State state) {
        return Mono.just(state)
                .cast(StateImpl.class)
                .flatMap(this.repository::save)
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
                    state.setNew(false);
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
                    state.setNew(false);
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
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

}
