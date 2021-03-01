package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Scope("singleton")
public class Candidate implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

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

    /* --------------------------------------------------- */

    public Candidate(
            StateService stateService,
            RaftProperties raftProperties,
            TimerHandler timerHandler,
            OutboundManager outboundManager
    ) {
        this.stateService = stateService;
        this.raftProperties = raftProperties;
        this.timerHandler = timerHandler;
        this.outboundManager = outboundManager;
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
    public void requestVote() {

    }

    @Override
    public void work() {

        log.info("Transited to CANDIDATE");

        // increments current term
        this.stateService.incrementCurrentTerm();

        // vote for myself
        String host = this.raftProperties.AddressToString(this.raftProperties.getHost());
        log.info(this.stateService.setVotedFor(host).toString());

        // issue RequestVote RPCs in parallel to each of the other servers in the cluster
        this.outboundManager.notifySubscribers(this);

        // set a candidate timeout
        this.setTimeout();
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
