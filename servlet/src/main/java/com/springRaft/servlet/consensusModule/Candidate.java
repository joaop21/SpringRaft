package com.springRaft.servlet.consensusModule;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Candidate implements RaftState {

    @Override
    public void appendEntries() {

    }

    @Override
    public void requestVote() {

    }

}