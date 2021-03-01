package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Follower implements RaftState {

    /* Timer handles for timeouts */
    private final TimerHandler timerHandler;

    /* Current timeout timer */
    private ScheduledFuture<?> scheduledFuture;

    /* --------------------------------------------------- */

    public Follower(TimerHandler timerHandler) {
        this.timerHandler = timerHandler;
        this.scheduledFuture = null;
    }

    /* --------------------------------------------------- */

    /**
     * Sets the value of the scheduledFuture instance variable.
     *
     * @param schedule Scheduled task.
     * */
    private void setScheduledFuture(ScheduledFuture<?> schedule) {
        this.scheduledFuture = schedule;
    }

    /* --------------------------------------------------- */

    @Override
    public void appendEntries() {

        // If receive an appendEntries remove the timer and set a new one
        this.timerHandler.cancelElectionTimeout(this.scheduledFuture);
        this.setTimeout();

    }

    @Override
    public void requestVote() {

    }

    @Override
    public void work() {
        this.setTimeout();
    }

    @Override
    public Message getNextMessage(String to) {
        return null;
    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        // schedule task
        ScheduledFuture<?> schedule = this.timerHandler.setElectionTimeout();

        // store runnable
        this.setScheduledFuture(schedule);

    }

}
