package com.springraft.raft.communication.outbound;

import com.springraft.raft.communication.message.AppendEntries;
import com.springraft.raft.communication.message.AppendEntriesReply;
import com.springraft.raft.communication.message.RequestVote;
import com.springraft.raft.communication.message.RequestVoteReply;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface OutboundStrategy {

    /**
     * Abstract method for the outbound strategies implement it, so they can send appendEntriesRPC.
     *
     * @param to Target server name.
     * @param message AppendEntries message to send.
     *
     * @return AppendEntriesReply Reply to handle internally.
     * */
    AppendEntriesReply appendEntries(String to, AppendEntries message) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Abstract method for the outbound strategies implement it, so they can send requestVoteRPC.
     *
     * @param to Target server name.
     * @param message RequestVote message to send.
     *
     * @return RequestVoteReply Reply to handle internally.
     * */
    RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Abstract method for the outbound strategies implement it, so they can send a general purpose
     * request to a server on a specific location.
     *
     * @param command String that encapsulates the request to invoke.
     * @param location String that represents the location of the server.
     *
     * @return Object Response object resulting of invoking the request on the specific server.
     * */
    Object request(String command, String location) throws InterruptedException, ExecutionException, URISyntaxException;

}