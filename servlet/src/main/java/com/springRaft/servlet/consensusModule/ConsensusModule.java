package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
@Getter
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* Index of the highest log entry known to be committed */
    private Long committedIndex;

    /* Term of the highest log entry known to be committed */
    private Long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private Long lastApplied;

    /* --------------------------------------------------- */

    public ConsensusModule(ApplicationContext applicationContext) {
        // Follower is the raft state where each server starts
        this.current = applicationContext.getBean(Follower.class);
        this.committedIndex = (long) 0;
        this.committedTerm = (long) 0;
        this.lastApplied = (long) 0;
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public void setCurrentState(RaftState state) {
        this.current = state;
        this.work();
    }

    /**
     * TODO
     * */
    public void incrementCommittedIndex() {
        this.committedIndex++;
    }

    /**
     * TODO
     * */
    public void incrementCommittedTerm() {
        this.committedTerm++;
    }

    /**
     * TODO
     * */
    public void incrementLastApplied() {
        this.lastApplied++;
    }

    /* --------------------------------------------------- */

    @Override
    public void appendEntries() {
        this.current.appendEntries();
    }

    @Override
    public void requestVote() {
        this.current.requestVote();
    }

    @Override
    public void work() {
        this.current.work();
    }

    @Override
    public Message getNextMessage(String to) {
        return this.current.getNextMessage(to);
    }

}
