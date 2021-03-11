package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

public interface InboundCommunication {
    AppendEntriesReply appendEntries(AppendEntries appendEntries);
    RequestVoteReply requestVote(RequestVote requestVote);
    void clientRequest(String command);
}
