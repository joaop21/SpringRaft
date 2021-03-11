package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.*;

public interface InboundCommunication {
    AppendEntriesReply appendEntries(AppendEntries appendEntries);
    RequestVoteReply requestVote(RequestVote requestVote);
    RequestReply clientRequest(String command);
}
