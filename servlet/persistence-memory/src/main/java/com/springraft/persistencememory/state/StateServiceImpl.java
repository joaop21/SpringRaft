package com.springraft.persistencememory.state;

import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StateServiceImpl implements StateService {

    /* Object that represents State */
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
    public State getState() {
        return this.state == null ? null : this.state.clone();
    }

    @Override
    public State saveState(State state) {
        this.state = ((StateImpl) state).clone();
        return this.state.clone();
    }

    /* --------------------------------------------------- */

    @Override
    public State newCandidateState() {
        this.state.setCurrentTerm(this.state.getCurrentTerm() + 1);
        this.state.setVotedFor(this.host);
        return this.saveState(state);
    }

    @Override
    public Long getCurrentTerm() {
        return this.state != null
                ? this.state.getCurrentTerm()
                : null;
    }

    @Override
    public String getVotedFor() {
        return this.state != null
                ? this.state.getVotedFor()
                : null;
    }

    @Override
    public State setVotedFor(String votedFor) {
        this.state.setVotedFor(votedFor);
        state.setVotedFor(votedFor);
        return this.saveState(state);
    }

    @Override
    public void setState(Long term, String votedFor) {
        this.state.setCurrentTerm(term);
        this.state.setVotedFor(votedFor);
        this.saveState(state);
    }

}
