package com.springraft.raft.stateMachine;

public interface CommitmentSubscriber {

    /**
     * Notification of new commit in Log State.
     * */
    void newCommit();

}
