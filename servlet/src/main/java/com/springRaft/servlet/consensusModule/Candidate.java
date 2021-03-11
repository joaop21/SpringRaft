package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.persistence.state.State;
import com.springRaft.servlet.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Candidate extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Current timeout timer */
    private ScheduledFuture<?> scheduledFuture;

    /* Message to send to the cluster requesting votes */
    private Message requestVoteMessage;

    /* Votes granted by the cluster */
    private int votesGranted;

    /* --------------------------------------------------- */

    public Candidate(
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
        this.requestVoteMessage = null;
        this.votesGranted = 0;
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

        // If receive AppendEntries replies when in candidate state there
        // is nothing to do

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);

        long currentTerm = this.stateService.getCurrentTerm();

        if(requestVote.getTerm() <= currentTerm) {

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

            // transit to follower state
            this.transitionManager.setNewFollowerState();

        }

        return reply;

    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

        if (requestVoteReply.getTerm() > this.stateService.getCurrentTerm()) {

            // update term
            this.stateService.setState(requestVoteReply.getTerm(), null);

            this.cleanBeforeTransit();

            // transit to follower state
            this.transitionManager.setNewFollowerState();

        }

        if (requestVoteReply.getVoteGranted()) {

            this.votesGranted++;

            if (this.votesGranted >= this.raftProperties.getQuorum()) {

                this.cleanBeforeTransit();

                // transit to leader state
                this.transitionManager.setNewLeaderState();

            }

        }

    }

    @Override
    public Message getNextMessage(String to) {

        return this.requestVoteMessage;

    }

    @Override
    public void start() {

        log.info("CANDIDATE");

        // set votes granted to none
        this.votesGranted = 0;

        // increments current term
        this.stateService.incrementCurrentTerm();

        // vote for myself
        String host = this.raftProperties.AddressToString(this.raftProperties.getHost());
        State state = this.stateService.setVotedFor(host);
        log.info(state.toString());
        this.votesGranted++;

        // build Request Vote Message
        LogState logState = this.logService.getState();

        this.requestVoteMessage =
                this.applicationContext.getBean(
                        RequestVote.class,
                        state.getCurrentTerm(),
                        this.raftProperties.AddressToString(this.raftProperties.getHost()),
                        logState.getCommittedIndex(),
                        logState.getCommittedTerm()
                        );

        // issue RequestVote RPCs in parallel to each of the other servers in the cluster
        this.outboundManager.newMessage();

        // set a candidate timeout
        this.setTimeout();
    }

    @Override
    public RequestReply clientRequest(String command) {

        // When in candidate state, there is nowhere to redirect the request or a leader to
        // handle them.

        return this.applicationContext.getBean(RequestReply.class, false, false, null);

        // probably we should store the requests, and redirect them when we became follower
        // or handle them when became leader

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

        this.cleanBeforeTransit();

        // reply with the current term
        reply.setTerm(appendEntries.getTerm());

        // check reply's success based on prevLogIndex and prevLogTerm
        // reply.setSuccess()
        // ...
        // ...
        // this need to be changed
        reply.setSuccess(true);

        // transit to follower state
        this.transitionManager.setNewFollowerState();

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
    private void cleanBeforeTransit() {

        // delete the existing scheduled task
        this.transitionManager.cancelScheduledTask(this.scheduledFuture);

        // change message to null and notify peer workers
        this.requestVoteMessage = null;
        this.outboundManager.newMessage();

    }

}
