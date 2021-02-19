package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.worker.ElectionTimeoutTimer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Follower implements RaftState {

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Pool for scheduled tasks */
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /* Current timeout timer */
    private ScheduledFuture<?> scheduledFuture;

    /* --------------------------------------------------- */

    public Follower(
            ApplicationContext applicationContext,
            RaftProperties raftProperties,
            @Qualifier(value = "timerTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler
    ) {
        this.applicationContext = applicationContext;
        this.raftProperties = raftProperties;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.scheduledFuture = null;
    }

    /* --------------------------------------------------- */

    private void setScheduledFuture(ScheduledFuture<?> schedule) {
        this.scheduledFuture = schedule;
    }

    /* --------------------------------------------------- */

    @Override
    public void appendEntries() {

        // If receive an appendEntries remove the timer and set a new one
        this.scheduledFuture.cancel(true);
        this.setTimeout();

    }

    @Override
    public void requestVote() {

    }

    @Override
    public void work() {
        this.setTimeout();
    }

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        ElectionTimeoutTimer timer = applicationContext.getBean(ElectionTimeoutTimer.class);
        Date date = new Date(System.currentTimeMillis() + this.raftProperties.getElectionTimeout().toMillis());

        // schedule task
        ScheduledFuture<?> schedule = this.threadPoolTaskScheduler.schedule(timer, date);

        // store runnable
        this.setScheduledFuture(schedule);

    }

}
