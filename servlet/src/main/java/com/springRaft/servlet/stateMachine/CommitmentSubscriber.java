package com.springRaft.servlet.stateMachine;

public interface CommitmentSubscriber {

    /**
     * Notification of new commit in Log State.
     * */
    void newCommit();

}
