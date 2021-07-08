package com.springraft.raft.communication.inbound;

import com.springraft.raft.communication.message.*;
import reactor.core.publisher.Mono;

public interface RaftInboundCommunication {

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
