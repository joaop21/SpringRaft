package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.util.Pair;
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
     * Setter method which sets the new state in consensus module.
     *
     * @param state The new raft state of this consensus module.
     * */
    public void setCurrentState(RaftState state) {
        this.current = state;
    }

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {
        return this.current.appendEntries(appendEntries);
    }

    @Override
    @Synchronized
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        this.current.appendEntriesReply(appendEntriesReply, from);
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
    public Pair<Message, Boolean> getNextMessage(String to) {
        return this.current.getNextMessage(to);
    }

    @Override
    public void start() {
        this.current.start();
    }

    @Override
    public RequestReply clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    /* --------------------------------------------------- */

    /**
     * Setter method which sets the new state in consensus module and starts that state.
     *
     * @param state The new raft state of this consensus module.
     * */
    @Synchronized
    public void start(RaftState state) {
        this.setCurrentState(state);
        this.start();
    }

}