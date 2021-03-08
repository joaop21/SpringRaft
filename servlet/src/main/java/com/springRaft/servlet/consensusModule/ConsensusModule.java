package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
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

    /* Index of the highest log entry known to be committed */
    private long committedIndex;

    /* Term of the highest log entry known to be committed */
    private long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private long lastApplied;

    /* --------------------------------------------------- */

    public ConsensusModule() {
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
        this.start();
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
    @Synchronized
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {
        return this.current.appendEntries(appendEntries);
    }

    @Override
    @Synchronized
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply) {
        this.current.appendEntriesReply(appendEntriesReply);
    }

    @Override
    @Synchronized
    public RequestVoteReply requestVote(RequestVote requestVote) {
        return this.current.requestVote(requestVote);
    }

    @Override
    @Synchronized
    public void requestVoteReply(RequestVoteReply requestVoteReply) {
        this.current.requestVoteReply(requestVoteReply);
    }

    @Override
    @Synchronized
    public Message getNextMessage(String to) {
        return this.current.getNextMessage(to);
    }

    @Override
    @Synchronized
    public void start() {
        this.current.start();
    }

}
