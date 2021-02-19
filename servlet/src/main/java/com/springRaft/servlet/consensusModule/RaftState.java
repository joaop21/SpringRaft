package com.springRaft.servlet.consensusModule;

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

}
