package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;

public interface InboundCommunication {
    Boolean appendEntries();
    RequestVoteReply requestVote(RequestVote requestVote);
}
