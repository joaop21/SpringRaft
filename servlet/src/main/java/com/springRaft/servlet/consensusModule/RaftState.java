package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;

public interface RaftState {

    /**
     * Method for handling AppendEntries RPC
     *
     * @param appendEntries AppendEntries object sent from leader.
     *
     * @return AppendEntriesReply Reply for the AppendEntries.
     * */
    AppendEntriesReply appendEntries(AppendEntries appendEntries);

    /**
     * Method for handling AppendEntries replies.
     *
     * @param appendEntriesReply AppendEntriesReply object sent from other servers.
     * */
    void appendEntriesReply(AppendEntriesReply appendEntriesReply);

    /**
     * Method for handling RequestVote RPC.
     *
     * @param requestVote RequestVote Object sent from a candidate.
     *
     * @return RequestVoteReply Reply for the vote request.
     * */
    RequestVoteReply requestVote(RequestVote requestVote);

    /**
     * Method for handling RequestVote replies.
     *
     * @param requestVoteReply RequestVoteReply object sent from other servers.
     */
    void requestVoteReply(RequestVoteReply requestVoteReply);

    /**
     * Method for getting the next message for a specific server.
     *
     * @param to String that represents the server.
     *
     * @return Message to send to the server.
     * */
    Message getNextMessage(String to);

    /**
     * Method for doing the work that it's required on startup.
     * */
    void start();

}
