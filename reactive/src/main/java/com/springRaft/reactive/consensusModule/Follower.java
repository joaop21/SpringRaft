package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.config.RaftProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

@Service
@Scope("singleton")
public class Follower extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Follower.class);

    /* Scheduled Runnable for state transition*/
    private Disposable scheduledTransition;

    /* Leader's ID so requests can be redirected */
    private String leaderId;

    /* --------------------------------------------------- */

    public Follower(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            TransitionManager transitionManager
    ) {
        super(applicationContext, consensusModule, raftProperties, transitionManager);
        this.scheduledTransition = null;
        this.leaderId = raftProperties.getHost();
    }

    /* --------------------------------------------------- */

    @Override
    public void start() {

        log.info("FOLLOWER");

        this.leaderId = this.raftProperties.getHost();

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