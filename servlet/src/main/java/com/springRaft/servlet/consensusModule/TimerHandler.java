package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.worker.ElectionTimeoutTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.OptionalLong;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Service
public class TimerHandler {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(TimerHandler.class);

    /* Context for getting the appropriate Beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Pool for scheduled tasks */
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /* --------------------------------------------------- */

    public TimerHandler(
            ApplicationContext applicationContext,
            RaftProperties raftProperties,
            @Qualifier(value = "timerTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler
    ) {
        this.applicationContext = applicationContext;
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

        ElectionTimeoutTimer timer = applicationContext.getBean(ElectionTimeoutTimer.class);

        Long timeout = this.getRandomLongBetweenRange(
                this.raftProperties.getElectionTimeoutMin().toMillis(),
                this.raftProperties.getElectionTimeoutMax().toMillis()
        );

        Date date = new Date(System.currentTimeMillis() + timeout);

        log.info("Set an election timeout: " + timeout + "ms");

        // schedule task
        return this.threadPoolTaskScheduler.schedule(timer, date);
    }

    /**
     * Cancels the scheduling of a specific scheduled task.
     *
     * @param scheduledFuture Scheduled task to cancel.
     * */
    public void cancelElectionTimeout(ScheduledFuture<?> scheduledFuture) {
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
