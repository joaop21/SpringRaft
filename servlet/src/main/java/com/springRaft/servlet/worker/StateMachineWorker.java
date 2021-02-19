package com.springRaft.servlet.worker;

import com.springRaft.servlet.consensusModule.ConsensusModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    /* Consensus Module to invoke functions */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    public StateMachineWorker(ApplicationContext applicationContext) {
        this.consensusModule = applicationContext.getBean(ConsensusModule.class);
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {
        this.consensusModule.work();
    }

}
