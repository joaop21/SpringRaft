package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

public interface RaftState {

    /**
     * Method for handling AppendEntries RPC
     * */
    void appendEntries();

    /**
     * Method for handling RequestVote RPC.
     *
     * @param requestVote RequestVote Object sent from a candidate.
     *
     * @return RequestVoteReply Reply for the vote request.
     * */
    RequestVoteReply requestVote(RequestVote requestVote);

    /**
     * TODO
     */
    void requestVoteReply(RequestVoteReply requestVoteReply);

    /**
     * Method for doing the work that it's required
     * */
    void work();

    /**
     * Method for getting the next message for a specific server.
     *
     * @param to String that represents the server.
     *
     * @return Message to send to the server.
     * */
    Message getNextMessage(String to);

}
