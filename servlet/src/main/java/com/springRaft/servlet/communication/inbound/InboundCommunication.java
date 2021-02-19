package com.springRaft.servlet.communication.inbound;

public interface InboundCommunication {
    Boolean appendEntries();
    Boolean requestVote();
}
