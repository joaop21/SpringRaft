package com.springRaft.reactive.persistence.state;

import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@Transactional
@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "R2DBC")
@AllArgsConstructor
public class R2DBCStateService implements StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(R2DBCStateService.class);

    /* Repository for State operations */
    private final R2DBCStateRepository repository;

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    @Override
    public Mono<State> getState() {
        return this.repository.findById((long) 1);
    }

    @Override
    public Mono<State> saveState(State state) {
        return this.repository.save(state)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<State> newCandidateState() {
        return this.getState()
                .flatMap(state -> {
                    state.setCurrentTerm(state.getCurrentTerm() + 1);
                    state.setVotedFor(this.raftProperties.getHost());
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<Long> getCurrentTerm() {
        return this.getState()
                .map(State::getCurrentTerm);
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
    public Mono<State> setVotedFor(String votedFor) {
        return this.getState()
                .flatMap(state -> {
                    state.setVotedFor(votedFor);
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

    @Override
    public Mono<State> setState(Long term, String votedFor) {
        return this.getState()
                .flatMap(state -> {
                    state.setCurrentTerm(term);
                    state.setVotedFor(votedFor);
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

}
