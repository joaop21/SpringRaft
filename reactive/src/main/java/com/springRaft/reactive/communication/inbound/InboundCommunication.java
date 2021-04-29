package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import reactor.core.publisher.Mono;

public interface InboundCommunication {
    Mono<RequestVoteReply> requestVote(RequestVote requestVote);
}
