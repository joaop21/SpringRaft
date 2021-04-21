package com.springRaft.reactive.communication.outbound;

public interface MessageSubscriber {

    /**
     * Notification of a new message for the subscribers.
     * */
    void newMessage();

    /**
     * Notification to clear the remaining messages for the subscribers.
     * */
    void clearMessages();

}
