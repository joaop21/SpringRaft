package com.springraft.raft.communication.outbound;

import com.springraft.raft.communication.message.AppendEntries;
import com.springraft.raft.communication.message.RequestVote;

public interface MessageSubscriber {

    /**
     * Method that invokes a RequestVoteRPC in every cluster member.
     *
     * @param requestVote Message to send.
     * */
    void sendRequestVote(RequestVote requestVote);

    /**
     * Method that invokes an AppendEntriesRPC in every cluster member, which works as an authority heartbeat so
     * every member in the cluster knows the new Leader.
     *
     * @param heartbeat Message to send.
     * */
    void sendAuthorityHeartbeat(AppendEntries heartbeat);

    /**
     * Method that invokes an AppendEntriesRPC in a specific server, which works as an heartbeat.
     *
     * @param heartbeat Message to send.
     * @param to String that represents the target server.
     * */
    void sendHeartbeat(AppendEntries heartbeat, String to);

    /**
     * Method that invokes an AppendEntriesRPC in a specific server.
     *
     * @param appendEntries Message to send.
     * @param to String that represents the target server.
     * */
    void sendAppendEntries(AppendEntries appendEntries, String to);

    /**
     * Method that marks a new client request.
     * */
    void newClientRequest();

    /**
     * Method thar marks a new Follower state in order to stop all the outgoing communications to the cluster.
     * */
    void newFollowerState();

}
