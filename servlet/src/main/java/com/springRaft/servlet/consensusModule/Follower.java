package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.log.Entry;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.persistence.state.StateService;
import com.springRaft.servlet.util.Pair;
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
            OutboundManager outboundManager
    ) {
        super(
                applicationContext, consensusModule,
                stateService, logService, raftProperties,
                transitionManager, outboundManager
        );
        this.scheduledFuture = null;
        this.leaderId = raftProperties.AddressToString(raftProperties.getHost());
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

            // begin new follower state and delete the existing task
            this.transitionManager.cancelScheduledTask(this.scheduledFuture);

            // set a new timeout, it's equivalent to transit to a new follower state
            this.setTimeout();

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

            // begin new follower state and delete the existing task
            this.transitionManager.cancelScheduledTask(this.scheduledFuture);

            // set a new timeout, it's equivalent to transit to a new follower state
            this.setTimeout();

        }

    }

    @Override
    public Pair<Message, Boolean> getNextMessage(String to) {
        return new Pair<>(null, false);
    }

    @Override
    public void start() {

        log.info("FOLLOWER");

        this.leaderId = this.raftProperties.AddressToString(this.raftProperties.getHost());

        this.setTimeout();

    }

    @Override
    public RequestReply clientRequest(String command) {

        // When in follower state, we need to redirect the request to the leader
        return this.applicationContext.getBean(RequestReply.class, false, true, this.leaderId);

    }

    /* --------------------------------------------------- */

    /**
     * A method that encapsulates replicated code, and has the function of setting
     * the reply for the received AppendEntries.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param reply AppendEntriesReply object, to send as response to the leader.
     * */
    protected void setAppendEntriesReply(AppendEntries appendEntries, AppendEntriesReply reply) {

        this.leaderId = appendEntries.getLeaderId();

        // remove the scheduled task
        this.transitionManager.cancelScheduledTask(this.scheduledFuture);

        // reply with the current term
        reply.setTerm(appendEntries.getTerm());

        // check reply's success based on prevLogIndex and prevLogTerm
        Entry entry = this.logService.getEntryByIndex(appendEntries.getPrevLogIndex());
        entry = entry == null ? new Entry((long) 0, (long) 0, null) : entry;

        if(entry.getIndex() == (long) appendEntries.getPrevLogIndex()) {

            if (entry.getTerm() == (long) appendEntries.getTerm()) {

                reply.setSuccess(true);

                this.applyAppendEntries(appendEntries);

            } else {

                reply.setSuccess(false);

            }

        } else {

            reply.setSuccess(false);

        }

        // set a new timeout, it's equivalent to transit to a new follower state
        this.setTimeout();

    }

    /**
     * Method for insert new entries in log and update the committed values in log state.
     *
     * @param appendEntries The received AppendEntries communication.
     * */
    private void applyAppendEntries(AppendEntries appendEntries) {

        if (appendEntries.getEntries().size() != 0) {

            // delete all the following conflict entries
            this.logService.deleteIndexesGreaterThan(appendEntries.getPrevLogIndex());

            // insert new entry
            Entry newEntry = new Entry(appendEntries.getTerm(), appendEntries.getEntries().get(0));
            newEntry = this.logService.insertEntry(newEntry);

            // update committed entries in LogState
            LogState logState = this.logService.getState();
            if (appendEntries.getLeaderCommit() > logState.getCommittedIndex()) {

                if (newEntry.getIndex() <= appendEntries.getLeaderCommit()) {

                    logState.setCommittedIndex(newEntry.getIndex());
                    logState.setCommittedTerm(newEntry.getTerm());
                    this.logService.saveState(logState);

                } else {

                    long index = appendEntries.getLeaderCommit();
                    Entry entry = this.logService.getEntryByIndex(index);
                    logState.setCommittedIndex(entry.getIndex());
                    logState.setCommittedTerm(entry.getTerm());

                }

            }

        }

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

}
