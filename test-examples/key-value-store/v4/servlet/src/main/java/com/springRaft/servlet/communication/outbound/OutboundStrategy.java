package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface OutboundStrategy {

    /**
     * TODO
     * */
    AppendEntriesReply appendEntries(String to, AppendEntries message) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * TODO
     * */
    RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * TODO
     * */
    Object request(String command, String location) throws InterruptedException, ExecutionException, URISyntaxException;

}
