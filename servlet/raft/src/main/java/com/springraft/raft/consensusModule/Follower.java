package com.springraft.raft.consensusModule;

import com.springraft.persistence.log.LogService;
import com.springraft.persistence.state.StateService;
import com.springraft.raft.communication.message.*;
import com.springraft.raft.communication.outbound.OutboundManager;
import com.springraft.raft.config.RaftProperties;
import com.springraft.raft.stateMachine.StateMachineWorker;
import com.springraft.raft.stateMachine.WaitingRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Follower extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Follower.class);

    /* Scheduled Thread */
    private ScheduledFuture<?> scheduledFuture;

    /* Leader's ID so requests can be redirected */
    private String leaderId;

    /* --------------------------------------------------- */

    public Follower(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            LogService logService,
            RaftProperties raftProperties,
            TransitionManager transitionManager,
            OutboundManager outboundManager,
            StateMachineWorker stateMachineWorker,
            WaitingRequests waitingRequests
    ) {
        super(
                applicationContext, consensusModule,
                stateService, logService, raftProperties,
                transitionManager, outboundManager,
                stateMachineWorker, waitingRequests
        );
        this.scheduledFuture = null;
        this.leaderId = raftProperties.getHost();
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
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        return super.appendEntries(appendEntries);

    }

    @Override
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {

        // if term is greater than mine, I should update it and transit to new follower
        if (appendEntriesReply.getTerm() > this.stateService.getCurrentTerm()) {

            // update term
            this.stateService.setState(appendEntriesReply.getTerm(), null);

            this.cleanBeforeTransit();

        }

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);

        long currentTerm = this.stateService.getCurrentTerm();

        if (requestVote.getTerm() < currentTerm) {

            // revoke request
            reply.setTerm(currentTerm);
            reply.setVoteGranted(false);

        } else if (requestVote.getTerm() > currentTerm) {

            // update term
            this.stateService.setState(requestVote.getTerm(), null);

            reply.setTerm(requestVote.getTerm());

            // check if candidate's log is at least as up-to-date as mine
            this.checkLog(requestVote, reply);

            this.cleanBeforeTransit();

        } else if (requestVote.getTerm() == currentTerm) {

            reply.setTerm(currentTerm);

            // check if candidate's log is at least as up-to-date as mine
            this.checkLog(requestVote, reply);

        }

        return reply;

    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

        // if term is greater than mine, I should update it and transit to new follower
        if (requestVoteReply.getTerm() > this.stateService.getCurrentTerm()) {

            // update term
            this.stateService.setState(requestVoteReply.getTerm(), null);

            this.cleanBeforeTransit();

        }

    }

    @Override
    public RequestReply clientRequest(String command) {

        // When in follower state, we need to redirect the request to the leader
        return this.applicationContext.getBean(RequestReply.class, false, new Object(), true, this.leaderId);

    }

    @Override
    public void start() {

        log.info("FOLLOWER");

        this.leaderId = this.raftProperties.getHost();

        this.setTimeout();

        this.outboundManager.newFollowerState();

    }

    /* --------------------------------------------------- */

    /**
     * Implementation of the postAppendEntries abstract method in parent class.
     * This method contains the behaviour to execute after invoking appendEntries method.
     *
     * @param appendEntries The received AppendEntries communication.
     * */
    @Override
    protected void postAppendEntries(AppendEntries appendEntries) {

        this.leaderId = appendEntries.getLeaderId();

        this.cleanBeforeTransit();

    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        // schedule task
        ScheduledFuture<?> schedule = this.transitionManager.setElectionTimeout();

        // store runnable
        this.setScheduledFuture(schedule);

    }

    /**
     * Method that cleans the volatile state before set a new timeout.
     * */
    private void cleanBeforeTransit() {

        // begin new follower state and delete the existing task
        this.transitionManager.cancelScheduledTask(this.scheduledFuture);

        // set a new timeout, it's equivalent to transit to a new follower state
        this.setTimeout();

    }

}

