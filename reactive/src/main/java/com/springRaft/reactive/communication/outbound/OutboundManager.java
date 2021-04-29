package com.springRaft.reactive.communication.outbound;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<Void> newMessage() {
        return Flux.fromIterable(this.subscribers)
                .flatMap(MessageSubscriber::newMessage)
                .then();
    }

    @Override
    public Mono<Void> clearMessages() {
        return Flux.fromIterable(this.subscribers)
                .flatMap(MessageSubscriber::clearMessages)
                .then();
    }
}
