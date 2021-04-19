package com.springRaft.reactive.consensusModule;

public interface RaftState {

    /**
     * Method for doing the work that it's required on startup.
     * */
    void start();

}
