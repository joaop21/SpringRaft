package com.springraft.raft.stateMachine;

import reactor.core.publisher.Mono;

public interface CommitmentSubscriber {

    /**
     * Notification of new commit in Log State.
     * */
    Mono<Void> newCommit();

}
