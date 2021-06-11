package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.message.*;
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

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of general requests.
     *
     * @param command String that contains the command to execute in the FSM.
     *
     * @return Mono<RequestReply> Reply ti send to the client who made the request.
     * */
    Mono<RequestReply> clientRequest(String command);
}
