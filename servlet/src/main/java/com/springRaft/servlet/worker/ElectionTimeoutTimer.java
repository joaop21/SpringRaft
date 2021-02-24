package com.springRaft.servlet.worker;

import com.springRaft.servlet.consensusModule.Candidate;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import com.springRaft.servlet.consensusModule.RaftState;
import com.springRaft.servlet.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
public class ElectionTimeoutTimer implements Runnable {

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Override
    public void run() {

        // increments current term
        this.stateService.incrementCurrentTerm();

        // transitions to candidate state
        RaftState candidate = applicationContext.getBean(Candidate.class);
        this.consensusModule.setCurrent(candidate);

    }

}
