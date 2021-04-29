package com.springRaft.reactive.persistence.state;

import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
@AllArgsConstructor
public class StateService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    /* Repository for State operations */
    private final StateRepository repository;

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    /**
     * Method for getting the current state from persistence mechanism.
     *
     * @return Mono<State> A mono with state.
     * */
    public Mono<State> getState() {
        return this.repository.findById((long) 1);
    }

    /**
     * Method for inserting or updating the current persisted state.
     *
     * @param state New state to insert/update.
     * @return Mono<State> New persisted state.
     * */
    public Mono<State> saveState(State state) {
        return this.repository.save(state)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    /**
     * Method for persist the new state after transit to candidate state.
     *
     * @return Mono<State> New state after transit to candidate state.
     * */
    public Mono<State> newCandidateState() {
        return this.getState()
                .flatMap(state -> {
                    state.setCurrentTerm(state.getCurrentTerm() + 1);
                    state.setVotedFor(this.raftProperties.getHost());
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

    /**
     * Method for getting the current state's term from persistence mechanism.
     *
     * @return Mono<Long> A mono with state's current term.
     * */
    public Mono<Long> getCurrentTerm() {
        return this.getState()
                .map(State::getCurrentTerm);
    }

    /**
     * Method for getting the current state's voted for from persistence mechanism.
     *
     * @return Mono<String> A mono with state's current votedFor.
     * */
    public Mono<String> getVotedFor() {
        return this.getState()
                .map(state ->
                    state.getVotedFor() == null
                            ? ""
                            : state.getVotedFor()
                );
    }

    /**
     * Method that sets and saves the new voted for.
     *
     * @param votedFor is the voted server name in the new term.
     *
     * @return Mono<State> A mono with the new persisted state.
     * */
    public Mono<State> setVotedFor(String votedFor) {
        return this.getState()
                .flatMap(state -> {
                    state.setVotedFor(votedFor);
                    state.setNew(false);
                    return this.saveState(state);
                });
    }

    /**
     * Method that sets and saves the new state.
     *
     * @param term is the new state's term.
     * @param votedFor is the voted server name in the new term.
     *
     * @return Mono<State> A mono with the new persisted state.
     * */
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
