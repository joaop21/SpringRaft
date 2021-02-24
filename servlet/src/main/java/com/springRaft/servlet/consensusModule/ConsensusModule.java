package com.springRaft.servlet.consensusModule;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class ConsensusModule {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* --------------------------------------------------- */

    public ConsensusModule(ApplicationContext applicationContext) {
        // Follower is the raft state where each server starts
        this.current = applicationContext.getBean(Follower.class);
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public void setCurrent(RaftState state) {
        this.current = state;
        this.work();
    }

    /* --------------------------------------------------- */

    public void appendEntries() {
        this.current.appendEntries();
    }

    public void requestVote() {
        this.current.requestVote();
    }

    public void work() {
        this.current.work();
    }

}
