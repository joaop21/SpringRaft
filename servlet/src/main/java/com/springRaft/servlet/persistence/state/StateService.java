package com.springRaft.servlet.persistence.state;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class StateService {

    /* Repository for State operations */
    private final StateRepository repository;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public State getState() {
        return this.repository
                .findById((long) 1)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public Long getCurrentTerm() {
        return this.repository
                .findById((long) 1)
                .map(State::getCurrentTerm)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public String getVotedFor() {
        return this.repository
                .findById((long) 1)
                .map(State::getVotedFor)
                .orElse(null);
    }

    /**
     * TODO
     * */
    public void incrementCurrentTerm() {
        State state = this.getState();
        state.setCurrentTerm(state.getCurrentTerm() + 1);
        this.repository.save(state);
    }

    /**
     * TODO
     * */
    public State setVotedFor(String votedFor) {
        State state = this.getState();
        state.setVotedFor(votedFor);
        return this.repository.save(state);
    }

    /**
     * TODO
     * */
    public void setState(Long term, String votedFor) {
        State state = this.getState();
        state.setCurrentTerm(term);
        state.setVotedFor(votedFor);
        this.repository.save(state);
    }

    /**
     * TODO
     * */
    public State saveState(State state) {
        return this.repository.save(state);
    }

}
