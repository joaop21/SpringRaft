package com.springRaft.servlet.communication.outbound;

public interface OutboundStrategy {
    Boolean appendEntries(String to);
    Boolean requestVote();
}
