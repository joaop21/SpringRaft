package com.springRaft.reactive.persistence.state;

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

    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    /* Repository for State operations */
    private final StateRepository repository;

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

}
