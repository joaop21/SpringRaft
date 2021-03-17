package com.springRaft.servlet.communication.outbound;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class OutboundManager {

    /* List of message subscribers */
    private final List<MessageSubscriber> subscribers = new ArrayList<>();

    /* --------------------------------------------------- */

    public void subscribe(MessageSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public void unsubscribe(MessageSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    public void newMessage() {
        for(MessageSubscriber subscriber : this.subscribers)
            subscriber.newMessage();
    }

    public void clearMessages() {
        for(MessageSubscriber subscriber : this.subscribers)
            subscriber.clearMessages();
    }

}
