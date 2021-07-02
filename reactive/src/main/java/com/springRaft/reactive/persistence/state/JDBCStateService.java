package com.springRaft.reactive.persistence.state;

import com.springRaft.reactive.config.RaftProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
@Scope("singleton")
@Transactional
@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "JDBC")
public class JDBCStateService implements StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(JDBCStateRepository.class);

    /* Repository for State operations */
    private final JDBCStateRepository repository;

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* Scheduler to execute database operations */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    public JDBCStateService(
            JDBCStateRepository repository,
            RaftProperties properties,
            @Qualifier("jdbcScheduler") Scheduler jdbcScheduler)
    {
        this.repository = repository;
        this.raftProperties = properties;
        this.scheduler = jdbcScheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<State> getState() {
        return Mono.fromCallable(() -> this.repository.findById((long) 1))
                .subscribeOn(this.scheduler)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }

    @Override
    public Mono<State> saveState(State state) {
        return Mono.fromCallable(() -> this.repository.save(state))
                .subscribeOn(this.scheduler)
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
