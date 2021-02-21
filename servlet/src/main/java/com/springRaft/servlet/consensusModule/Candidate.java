package com.springRaft.servlet.consensusModule;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Candidate implements RaftState {

    @Override
    public void appendEntries() {

    }

    @Override
    public void requestVote() {

    }

    @Override
    public void work() {
        System.out.println("CANDIDATE");
    }

}
