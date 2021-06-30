package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.RequestVote;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("singleton")
public class OutboundManager implements MessageSubscriber {

    /* List of message subscribers */
    private final List<MessageSubscriber> subscribersList = new ArrayList<>();

    /* Map of message subscribers */
    private final Map<String,MessageSubscriber> subscribers = new HashMap<>();

    /* --------------------------------------------------- */

    public void subscribe(String server, MessageSubscriber subscriber) {
        if (this.subscribers.putIfAbsent(server,subscriber) == null)
            this.subscribersList.add(subscriber);
    }

    /* --------------------------------------------------- */

    @Override
    public void sendRequestVote(RequestVote requestVote) {
        for(MessageSubscriber subscriber : this.subscribersList)
            subscriber.sendRequestVote(requestVote);
    }

    @Override
    public void sendAuthorityHeartbeat(AppendEntries heartbeat) {
        for(MessageSubscriber subscriber : this.subscribersList)
            subscriber.sendAuthorityHeartbeat(heartbeat);
    }

    @Override
    public void sendHeartbeat(AppendEntries heartbeat, String to) {
        this.subscribers.get(to).sendHeartbeat(heartbeat, to);
    }

    @Override
    public void sendAppendEntries(AppendEntries appendEntries, String to) {
        this.subscribers.get(to).sendAppendEntries(appendEntries, to);
    }

    @Override
    public void newClientRequest() {
        for(MessageSubscriber subscriber : this.subscribersList)
            subscriber.newClientRequest();
    }

    @Override
    public void newFollowerState() {
        for(MessageSubscriber subscriber : this.subscribersList)
            subscriber.newFollowerState();
    }

}
