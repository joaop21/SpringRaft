package com.springraft.persistencememory.state;

import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
public class StateServiceImpl implements StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateServiceImpl.class);

    /* Repository for State operations */
    private StateImpl state;

    /* Raft property that need to be accessed */
    private final String host;

    /* --------------------------------------------------- */

    public StateServiceImpl(
            @Value("${raft.hostname}") String host
    ) {
        this.state = null;
        this.host = host;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<State> getState() {
        return Mono.create(sink -> sink.success(this.state == null ? null : this.state.clone()));
    }

    @Override
    public Mono<? extends State> saveState(State state) {
        return Mono.just(state)
                .cast(StateImpl.class)
                .map(StateImpl::clone)
                .doOnNext(state1 -> this.state = state1)
                .flatMap(state1 -> Mono.just(this.state.clone()));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<State> newCandidateState() {
        return this.getState()
                .cast(StateImpl.class)
                .flatMap(state -> {
                    state.setCurrentTerm(state.getCurrentTerm() + 1);
                    state.setVotedFor(this.host);
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

