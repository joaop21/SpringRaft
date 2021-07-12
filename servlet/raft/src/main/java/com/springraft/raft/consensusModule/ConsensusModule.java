package com.springraft.raft.consensusModule;

import com.springraft.raft.communication.message.*;
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

    /**
     * Setter method which sets the new state in consensus module and starts that state.
     *
     * @param state The new raft state of this consensus module.
     * */
    @Synchronized
    public void setAndStartNewState(RaftState state) {
        this.setCurrentState(state);
        this.start();
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
    public RequestReply clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    public void start() {
        this.current.start();
    }

}
