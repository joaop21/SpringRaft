package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.RequestVote;
import reactor.core.publisher.Mono;

public interface MessageSubscriber {

    /**
     * TODO
     * */
    Mono<Void> sendRequestVote(RequestVote requestVote);

    /**
     * TODO
     * */
    Mono<Void> sendAuthorityHeartbeat(AppendEntries heartbeat);

    /**
     * TODO
     * */
    Mono<Void> sendHeartbeat(AppendEntries heartbeat, String to);

    /**
     * TODO
     * */
    Mono<Void> sendAppendEntries(AppendEntries appendEntries, String to);

    /**
     * TODO
     * */
    Mono<Void> newClientRequest();

    /**
     * TODO
     * */
    Mono<Void> newFollowerState();

}
