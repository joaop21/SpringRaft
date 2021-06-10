package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.RequestVote;
import reactor.core.publisher.Mono;

public interface MessageSubscriber {

    Mono<Void> sendRequestVote(RequestVote requestVote);

    Mono<Void> sendAuthorityHeartbeat(AppendEntries heartbeat);

    Mono<Void> sendHeartbeat(AppendEntries heartbeat, String to);

    Mono<Void> sendAppendEntries(AppendEntries appendEntries, String to);

    Mono<Void> newClientRequest();

    Mono<Void> newFollowerState();

}
