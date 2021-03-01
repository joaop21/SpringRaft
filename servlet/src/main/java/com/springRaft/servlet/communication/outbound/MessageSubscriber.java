package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.consensusModule.RaftState;

public interface MessageSubscriber {
    void newMessage();
}
