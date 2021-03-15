package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.worker.StateTransition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.OptionalLong;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Service
public class TransitionManager {

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Pool for scheduled tasks */
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /* --------------------------------------------------- */

    public TransitionManager(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            @Qualifier(value = "transitionTaskExecutor") ThreadPoolTaskScheduler threadPoolTaskScheduler
    ) {
        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    /* --------------------------------------------------- */

    /**
     * Creates a scheduled task, based on raft properties.
     *
     * @return ScheduledFuture<?> Scheduled task.
     * */
    public ScheduledFuture<?> setElectionTimeout() {

        StateTransition transition = applicationContext
                .getBean(StateTransition.class, applicationContext, consensusModule, Candidate.class);

        Long timeout = this.getRandomLongBetweenRange(
                this.raftProperties.getElectionTimeoutMin().toMillis(),
                this.raftProperties.getElectionTimeoutMax().toMillis()
        );

        Date date = new Date(System.currentTimeMillis() + timeout);

        // schedule task
        return this.threadPoolTaskScheduler.schedule(transition, date);
    }

    /**
     * TODO
     * */
    public void setNewFollowerState() {

        StateTransition transition = applicationContext
                .getBean(StateTransition.class, applicationContext, consensusModule, Follower.class);

        this.threadPoolTaskScheduler.schedule(transition, new Date());

    }

    /**
     * TODO
     * */
    public void setNewLeaderState() {

        StateTransition transition = applicationContext
                .getBean(StateTransition.class, applicationContext, consensusModule, Leader.class);

        this.threadPoolTaskScheduler.schedule(transition, new Date());

    }

    /**
     * Cancels the scheduling of a specific scheduled task.
     *
     * @param scheduledFuture Scheduled task to cancel.
     * */
    public void cancelScheduledTask(ScheduledFuture<?> scheduledFuture) {
        scheduledFuture.cancel(true);
    }

    /* --------------------------------------------------- */

    /**
     * Calculates a random long between a minimum and a maximum.
     *
     * @param min Minimum long in the range.
     * @param max Maximum long in the range.
     *
     * @return Long Random calculated Long.
     * */
    private Long getRandomLongBetweenRange(long min, long max){

        OptionalLong op = new Random().longs(min,(max+1)).findFirst();

        return op.isPresent() ? op.getAsLong() : min;
    }
}
