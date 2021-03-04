package com.springRaft.servlet.communication.outbound;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope("singleton")
@AllArgsConstructor
public class OutboundManager {

    /* List of message subscribers */
    private final List<MessageSubscriber> subscribers;

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

}