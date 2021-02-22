package com.springRaft.servlet.persistence.state;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StateService {

    /* Repository for State operations */
    private final StateRepository repository;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public State getState() {
        return repository
                .findById((long) 1)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public Long getCurrentTerm() {
        return repository
                .findById((long) 1)
                .map(State::getCurrentTerm)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public String getVotedFor() {
        return repository
                .findById((long) 1)
                .map(State::getVotedFor)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public State saveState(State state) {
        return repository.save(state);
    }

}
