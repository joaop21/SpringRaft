package com.springRaft.servlet.consensusModule;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Candidate implements RaftState {

    @Override
    public void appendEntries() {

        // If the leader’s term (included in its RPC) is at least
        //as large as the candidate’s current term, then the candidate
        //recognizes the leader as legitimate and returns to follower
        //state.
        // ...

        // If the term in the RPC is smaller than the candidate’s
        //current term, then the candidate rejects the RPC and
        // continues in candidate state.
        // ...

    }

    @Override
    public void requestVote() {

    }

    @Override
    public void work() {
        System.out.println("CANDIDATE");

        // votes for myself

        // issue RequestVote RPCs in parallel to each of the other servers in the cluster

        // set a candidate timeout
    }



}
