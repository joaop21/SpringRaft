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

    @Override
    public void clientRequest(String command) {
        this.current.clientRequest(command);
    }

}
