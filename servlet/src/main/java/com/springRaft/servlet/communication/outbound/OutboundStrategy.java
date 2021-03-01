package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface OutboundStrategy {
    Boolean appendEntries(String to);
    RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException;
}
