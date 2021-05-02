package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.AppendEntriesReply;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import reactor.core.publisher.Mono;

public interface InboundCommunication {

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of the appendEntriesRPC.
     *
     * @param appendEntries Message received in AppendEntriesRPC.
     *
     * @return Mono<AppendEntriesReply> Reply to send to the server which invoke the communication.
     * */
    Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries);

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of the requestVoteRPC.
     *
     * @param requestVote Message received in requestVoteRPC.
     *
     * @return Mono<RequestVoteReply> Reply to send to the server which invoke the communication.
     * */
    Mono<RequestVoteReply> requestVote(RequestVote requestVote);
}
