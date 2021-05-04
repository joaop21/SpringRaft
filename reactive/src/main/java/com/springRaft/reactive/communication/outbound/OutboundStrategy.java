package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.AppendEntriesReply;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import reactor.core.publisher.Mono;

public interface OutboundStrategy {

    /**
     * Abstract method for the outbound strategies implement it, so they can send appendEntriesRPC.
     *
     * @param to Target server name.
     * @param message AppendEntries message to send.
     *
     * @return Mono<AppendEntriesReply> Reply to handle internally.
     * */
    Mono<AppendEntriesReply> appendEntries(String to, AppendEntries message);

    /**
     * Abstract method for the outbound strategies implement it, so they can send requestVoteRPC.
     *
     * @param to Target server name.
     * @param message RequestVote message to send.
     *
     * @return Mono<RequestVoteReply> Reply to handle internally.
     * */
    Mono<RequestVoteReply> requestVote(String to, RequestVote message);

    /**
     * Abstract method for the outbound strategies implement it, so they can send a general purpose
     * request to a server on a specific location.
     *
     * @param command String that encapsulates the request to invoke.
     * @param location String that represents the location of the server.
     *
     * @return Mono<?> Response object resulting of invoking the request on the specific server.
     * */
    Mono<?> request(String command, String location);

}
