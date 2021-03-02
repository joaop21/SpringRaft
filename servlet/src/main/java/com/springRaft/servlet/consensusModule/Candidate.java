package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
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
public class Candidate implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Timer handles for timeouts */
    private final TimerHandler timerHandler;

    /* Current timeout timer */
    private ScheduledFuture<?> scheduledFuture;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* Message to send to the cluster requesting votes */
    private Message requestVoteMessage;

    /* --------------------------------------------------- */

    public Candidate(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            RaftProperties raftProperties,
            TimerHandler timerHandler,
            OutboundManager outboundManager
    ) {
        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.stateService = stateService;
        this.raftProperties = raftProperties;
        this.timerHandler = timerHandler;
        this.outboundManager = outboundManager;
        this.scheduledFuture = null;
        this.requestVoteMessage = null;
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

        // If the leader’s term (included in its RPC) is at least
        //as large as the candidate’s current term, then the candidate
        //recognizes the leader as legitimate and returns to follower
        //state.
        // ...

        // If the term in the RPC is smaller than the candidate’s
        //current term, then the candidate rejects the RPC and
        // continues in candidate state.
        // ...

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);

        long currentTerm = this.stateService.getCurrentTerm();

        if(requestVote.getTerm() < currentTerm) {

            // revoke request
            reply.setTerm(currentTerm);
            reply.setVoteGranted(false);

        } else if (requestVote.getTerm() > currentTerm) {

            // update term & vote for this request
            this.stateService.setState(requestVote.getTerm(), requestVote.getCandidateId());

            // begin new follower state and delete the existing timer
            this.timerHandler.cancelScheduledTask(this.scheduledFuture);

            // change message to null and notify peer workers
            this.requestVoteMessage = null;
            this.outboundManager.newMessage();

            // transit to follower state
            this.setNewFollower();

            reply.setTerm(requestVote.getTerm());
            reply.setVoteGranted(true);

        } else if (requestVote.getTerm() == currentTerm) {

            reply.setTerm(currentTerm);
            reply.setVoteGranted(false);

        }

        return reply;

    }

    @Override
    public void work() {

        log.info("NEW ELECTION");

        // increments current term
        this.stateService.incrementCurrentTerm();

        // vote for myself
        String host = this.raftProperties.AddressToString(this.raftProperties.getHost());
        State state = this.stateService.setVotedFor(host);
        log.info(state.toString());

        // build Request Vote Message
        this.requestVoteMessage =
                this.applicationContext.getBean(
                        RequestVote.class,
                        state.getCurrentTerm(),
                        this.raftProperties.AddressToString(this.raftProperties.getHost()),
                        this.consensusModule.getCommittedIndex(),
                        this.consensusModule.getCommittedTerm()
                        );

        // issue RequestVote RPCs in parallel to each of the other servers in the cluster
        this.outboundManager.newMessage();

        // set a candidate timeout
        this.setTimeout();
    }

    @Override
    public Message getNextMessage(String to) {
        return this.requestVoteMessage;
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

    /**
     * Set a new follower scheduled thread.
     * */
    private void setNewFollower() {

        // schedule task
        ScheduledFuture<?> schedule = this.timerHandler.setNewFollowerState();

        // store runnable
        this.setScheduledFuture(schedule);

    }

}
