package com.springRaft.servlet.communication.outbound;

public interface OutboundCommunication {
    Boolean appendEntries();
    Boolean requestVote();
}
