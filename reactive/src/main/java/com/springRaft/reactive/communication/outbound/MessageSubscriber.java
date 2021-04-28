package com.springRaft.reactive.communication.outbound;

import reactor.core.publisher.Mono;

public interface MessageSubscriber {

    /**
     * Notification of a new message for the subscribers.
     *
     * @return Result it's not important.
     * */
    Mono<Void> newMessage();

    /**
     * Notification to clear the remaining messages for the subscribers.
     *
     * @return Result it's not important.
     * */
    Mono<Void> clearMessages();

}
