package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.RequestVote;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope("singleton")
public class OutboundManager implements MessageSubscriber {

    /* Map of message subscribers */
    private final Map<String,MessageSubscriber> subscribers = new HashMap<>();

    private final List<MessageSubscriber> subscribersList = new ArrayList<>();

    /* --------------------------------------------------- */

    public void subscribe(String server, MessageSubscriber subscriber) {
        if (this.subscribers.putIfAbsent(server,subscriber) == null)
            this.subscribersList.add(subscriber);
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> sendRequestVote(RequestVote requestVote) {
        return Flux.fromIterable(this.subscribersList)
                .flatMap(subscriber -> subscriber.sendRequestVote(requestVote))
                .then();
    }

    @Override
    public Mono<Void> sendAuthorityHeartbeat(AppendEntries heartbeat) {
        return Flux.fromIterable(this.subscribersList)
                .flatMap(subscriber -> subscriber.sendAuthorityHeartbeat(heartbeat))
                .then();
    }

    @Override
    public Mono<Void> sendHeartbeat(AppendEntries heartbeat, String to) {
        return Mono.just(this.subscribers.get(to))
                .flatMap(subscriber -> subscriber.sendHeartbeat(heartbeat,to));
    }

    @Override
    public Mono<Void> sendAppendEntries(AppendEntries appendEntries, String to) {
        return Mono.just(this.subscribers.get(to))
                .flatMap(subscriber -> subscriber.sendAppendEntries(appendEntries,to));
    }

    @Override
    public Mono<Void> newClientRequest() {
        return Flux.fromIterable(this.subscribersList)
                .flatMap(MessageSubscriber::newClientRequest)
                .then();
    }

    @Override
    public Mono<Void> newFollowerState() {
        return Flux.fromIterable(this.subscribersList)
                .flatMap(MessageSubscriber::newFollowerState)
                .then();
    }

}
