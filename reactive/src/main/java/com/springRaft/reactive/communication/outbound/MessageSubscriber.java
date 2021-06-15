package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.RequestVote;
import reactor.core.publisher.Mono;

public interface MessageSubscriber {

    /**
     * Method that invokes a RequestVoteRPC in every cluster member.
     *
     * @param requestVote Message to send.
     * */
    Mono<Void> sendRequestVote(RequestVote requestVote);

    /**
     * Method that invokes an AppendEntriesRPC in every cluster member, which works as an authority heartbeat so
     * every member in the cluster knows the new Leader.
     *
     * @param heartbeat Message to send.
     * */
    Mono<Void> sendAuthorityHeartbeat(AppendEntries heartbeat);

    /**
     * Method that invokes an AppendEntriesRPC in a specific server, which works as an heartbeat.
     *
     * @param heartbeat Message to send.
     * @param to String that represents the target server.
     * */
    Mono<Void> sendHeartbeat(AppendEntries heartbeat, String to);

    /**
     * Method that invokes an AppendEntriesRPC in a specific server.
     *
     * @param appendEntries Message to send.
     * @param to String that represents the target server.
     * */
    Mono<Void> sendAppendEntries(AppendEntries appendEntries, String to);

    /**
     * Method that marks a new client request.
     * */
    Mono<Void> newClientRequest();

    /**
     * Method thar marks a new Follower state in order to stop all the outgoing communications to the cluster.
     * */
    Mono<Void> newFollowerState();

}
