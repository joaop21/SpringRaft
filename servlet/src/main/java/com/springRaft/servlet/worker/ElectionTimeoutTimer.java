package com.springRaft.servlet.worker;

import com.springRaft.servlet.consensusModule.Candidate;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import com.springRaft.servlet.consensusModule.RaftState;
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

    /* --------------------------------------------------- */

    @Override
    public void run() {
        System.out.println("TIMEOUT!!");

        // increments current term
        // ...

        // transitions to candidate state
        RaftState candidate = applicationContext.getBean(Candidate.class);
        this.consensusModule.setCurrent(candidate);
    }

}
