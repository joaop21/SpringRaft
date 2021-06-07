package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

@AllArgsConstructor
public abstract class RaftStateContext {

    /* Application Context for getting beans */
    protected final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    protected final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    //protected final StateService stateService;

    /* Service to access persisted log repository */
    //protected final LogService logService;

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* --------------------------------------------------- */

}
