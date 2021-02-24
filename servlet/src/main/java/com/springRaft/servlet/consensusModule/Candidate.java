package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
@AllArgsConstructor
public class Candidate implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Consensus Module for invoking the necessary functions */
    private final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* --------------------------------------------------- */

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

        log.info("Transited to CANDIDATE");

        // increments current term
        this.stateService.incrementCurrentTerm();

        // votes for myself
        // System.out.println(this.stateService.setVotedFor("Me").toString());

        // issue RequestVote RPCs in parallel to each of the other servers in the cluster

        // set a candidate timeout
    }



}
