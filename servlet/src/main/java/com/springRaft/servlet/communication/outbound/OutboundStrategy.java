package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface OutboundStrategy {
    AppendEntriesReply appendEntries(String to, AppendEntries message) throws InterruptedException, ExecutionException, TimeoutException;
    RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException;
}
