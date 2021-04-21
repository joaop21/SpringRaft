package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

@Service
@Scope("singleton")
public class Candidate extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Scheduled Runnable for state transition*/
    private Disposable scheduledTransition;

    /* --------------------------------------------------- */

    public Candidate(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            RaftProperties raftProperties,
            TransitionManager transitionManager
    ) {
        super(applicationContext, consensusModule, stateService, raftProperties, transitionManager);
        this.scheduledTransition = null;
    }

    /* --------------------------------------------------- */

    @Override
    public void start() {

        log.info("CANDIDATE");

        // persist new state
        this.stateService.newCandidateState()
                .doOnSuccess(state -> log.info(state.toString()))
                .subscribe();

        // set a candidate timeout
        this.setTimeout();
    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        this.scheduledTransition = this.transitionManager.setElectionTimeout().subscribe();

    }
}
