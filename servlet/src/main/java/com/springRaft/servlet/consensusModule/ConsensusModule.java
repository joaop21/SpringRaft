package com.springRaft.servlet.consensusModule;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class ConsensusModule {

    /* Current Raft state - Follower, Candidate, Leader */
    private final RaftState current;

    /* --------------------------------------------------- */

    public ConsensusModule(ApplicationContext applicationContext) {

        // Follower is the raft state where each server starts
        this.current = applicationContext.getBean(Follower.class);

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
