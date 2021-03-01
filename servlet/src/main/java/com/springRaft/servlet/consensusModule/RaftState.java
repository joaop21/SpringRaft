package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;

public interface RaftState {

    /**
     * Method for handling AppendEntries RPC
     * */
    void appendEntries();

    /**
     * Method for handling RequestVote RPC
     * */
    void requestVote();

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
