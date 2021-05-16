package com.springRaft.reactive.worker;

import com.springRaft.reactive.consensusModule.ConsensusModule;
import com.springRaft.reactive.consensusModule.RaftState;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
public class StateTransition implements Runnable {

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* State to which will transit */
    private final Class<RaftState> state;

    /* --------------------------------------------------- */

    @Override
    public void run() {

        // transitions to candidate state
        RaftState raftState = applicationContext.getBean(state);
        this.consensusModule.setAndStartNewState(raftState).subscribe();

    }

}
