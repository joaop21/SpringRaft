package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Leader implements RaftState {

    @Override
    public void appendEntries() {

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        return null;
    }

    @Override
    public void work() {

    }

    @Override
    public Message getNextMessage(String to) {
        return null;
    }

}
