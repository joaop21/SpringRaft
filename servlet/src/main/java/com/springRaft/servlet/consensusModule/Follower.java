package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Follower implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Follower.class);

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* Transition handler */
    private final TransitionManager transitionManager;

    /* Scheduled Thread */
    private ScheduledFuture<?> scheduledFuture;

    /* --------------------------------------------------- */

    public Follower(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            TransitionManager transitionManager
    ) {
        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.stateService = stateService;
        this.transitionManager = transitionManager;
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
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        AppendEntriesReply reply = this.applicationContext.getBean(AppendEntriesReply.class);

        long currentTerm = this.stateService.getCurrentTerm();

        if (appendEntries.getTerm() < currentTerm) {

            reply.setTerm(currentTerm);
            reply.setSuccess(false);

        } else if (appendEntries.getTerm() > currentTerm) {

            // update term
            this.stateService.setState(appendEntries.getTerm(), null);

            this.setAppendEntriesReply(appendEntries, reply);

        } else if (appendEntries.getTerm() == currentTerm) {

            this.setAppendEntriesReply(appendEntries, reply);

        }

        return reply;

    }

    @Override
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply) {

        // If receive AppendEntries replies when in follower state there
        // is nothing to do

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

            if (reply.getVoteGranted()) {

                // begin new follower state and delete the existing task
                this.transitionManager.cancelScheduledTask(this.scheduledFuture);

                // set a new timeout, it's equivalent to transit to a new follower state
                this.setTimeout();

            }

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

            // begin new follower state and delete the existing task
            this.transitionManager.cancelScheduledTask(this.scheduledFuture);

            // set a new timeout, it's equivalent to transit to a new follower state
            this.setTimeout();

        }

    }

    @Override
    public Message getNextMessage(String to) {
        return null;
    }

    @Override
    public void start() {

        log.info("FOLLOWER");

        this.setTimeout();

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
     * TODO
     * */
    private void checkLog(RequestVote requestVote, RequestVoteReply reply) {

        if (requestVote.getLastLogTerm() > this.consensusModule.getCommittedTerm()) {

            // vote for this request if not voted for anyone yet
            this.setVote(requestVote, reply);

        } else if (requestVote.getLastLogTerm() < this.consensusModule.getCommittedTerm()) {

            // revoke request
            reply.setVoteGranted(false);

        } else if (requestVote.getLastLogTerm() == this.consensusModule.getCommittedTerm()) {

            if (requestVote.getLastLogIndex() >= this.consensusModule.getCommittedIndex()) {

                // vote for this request if not voted for anyone yet
                this.setVote(requestVote, reply);

            } else if (requestVote.getLastLogIndex() < this.consensusModule.getCommittedIndex()) {

                // revoke request
                reply.setVoteGranted(false);

            }

        }

    }

    /**
     * TODO
     * */
    private void setVote(RequestVote requestVote, RequestVoteReply reply) {

        String votedFor = this.stateService.getVotedFor();

        if (votedFor == null || votedFor.equals(requestVote.getCandidateId())) {

            this.stateService.setVotedFor(requestVote.getCandidateId());
            reply.setVoteGranted(true);

        } else {

            reply.setVoteGranted(false);

        }

    }

    /**
     * A method that encapsulates replicated code, and has the function of setting
     * the reply for the received AppendEntries.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param reply AppendEntriesReply object, to send as response to the leader.
     * */
    private void setAppendEntriesReply(AppendEntries appendEntries, AppendEntriesReply reply) {

        // remove the scheduled task
        this.transitionManager.cancelScheduledTask(this.scheduledFuture);

        // set a new timeout, it's equivalent to transit to a new follower state
        this.setTimeout();

        // reply with the current term
        reply.setTerm(appendEntries.getTerm());

        // check reply's success based on prevLogIndex and prevLogTerm
        // reply.setSuccess()
        // ...
        // ...
        // this need to be changed
        reply.setSuccess(true);

    }

}
