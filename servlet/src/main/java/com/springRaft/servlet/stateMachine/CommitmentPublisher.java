package com.springRaft.servlet.stateMachine;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class CommitmentPublisher {

    /* List of message subscribers */
    private final List<CommitmentSubscriber> subscribers = new ArrayList<>();

    /* --------------------------------------------------- */

    public void subscribe(CommitmentSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public void unsubscribe(CommitmentSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    public void newCommit() {
        for (CommitmentSubscriber subscriber : this.subscribers)
            subscriber.newCommit();
    }

}
