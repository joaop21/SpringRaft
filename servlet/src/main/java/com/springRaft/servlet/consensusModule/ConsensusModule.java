package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
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
    private long committedIndex;

    /* Term of the highest log entry known to be committed */
    private long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private long lastApplied;

    /* --------------------------------------------------- */

    public ConsensusModule(ApplicationContext applicationContext) {
        this.current = null;
        this.committedIndex = 0;
        this.committedTerm = 0;
        this.lastApplied = 0;
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
    public RequestVoteReply requestVote(RequestVote requestVote) {
        return this.current.requestVote(requestVote);
    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {
        this.current.requestVoteReply(requestVoteReply);
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
