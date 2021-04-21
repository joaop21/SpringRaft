package com.springRaft.reactive.communication.outbound;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class OutboundManager implements MessageSubscriber {

    /* List of message subscribers */
    private final List<MessageSubscriber> subscribers = new ArrayList<>();

    /* --------------------------------------------------- */

    public void subscribe(MessageSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    /* --------------------------------------------------- */

    @Override
    public void newMessage() {
        Flux.fromIterable(this.subscribers)
                .doOnNext(MessageSubscriber::newMessage)
                .subscribe();
    }

    @Override
    public void clearMessages() {
        Flux.fromIterable(this.subscribers)
                .doOnNext(MessageSubscriber::clearMessages)
                .subscribe();
    }
}
