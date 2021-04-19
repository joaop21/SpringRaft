package com.springRaft.reactive.consensusModule;

import lombok.Getter;
import lombok.Synchronized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
@Getter
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* --------------------------------------------------- */

    public ConsensusModule() {
        this.current = null;
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public void setCurrentState(RaftState state) {
        this.current = state;
        this.start();
    }

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public void start() {
        this.current.start();
    }

}
