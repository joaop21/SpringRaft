package com.springraft.persistencejpa.state;

import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StateServiceImpl implements StateService {

    /* Repository for State operations */
    private final StateRepository repository;

    /* Raft property that need to be accessed */
    private final String host;

    /* --------------------------------------------------- */

    public StateServiceImpl(
            StateRepository repository,
            @Value("${raft.hostname}") String host
    ) {
        this.repository = repository;
        this.host = host;
    }

    /* --------------------------------------------------- */

    @Override
    public State getState() {
        return this.repository
                .findById((long) 1)
                .orElse(null);
    }

    @Override
    public State saveState(State state) {
        return this.repository.save((StateImpl) state);
    }

    /* --------------------------------------------------- */

    @Override
    public State newCandidateState() {
        StateImpl state = (StateImpl) this.getState();
        state.setCurrentTerm(state.getCurrentTerm() + 1);
        state.setVotedFor(this.host);
        return this.saveState(state);
    }

    @Override
    public Long getCurrentTerm() {
        return this.repository
                .findById((long) 1)
                .map(State::getCurrentTerm)
                .orElse(null);
    }

    @Override
    public String getVotedFor() {
        return this.repository
                .findById((long) 1)
                .map(State::getVotedFor)
                .orElse(null);
    }

    @Override
    public State setVotedFor(String votedFor) {
        StateImpl state = (StateImpl) this.getState();
        state.setVotedFor(votedFor);
        return this.repository.save(state);
    }

    @Override
    public void setState(Long term, String votedFor) {
        StateImpl state = (StateImpl) this.getState();
        state.setCurrentTerm(term);
        state.setVotedFor(votedFor);
        this.repository.save(state);
    }

}
