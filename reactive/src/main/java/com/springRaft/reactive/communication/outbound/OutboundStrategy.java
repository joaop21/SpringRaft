package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import reactor.core.publisher.Mono;

public interface OutboundStrategy {

    /**
     * TODO
     * */
    Mono<RequestVoteReply> requestVote(String to, RequestVote message);

}
