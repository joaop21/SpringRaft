package com.springraft.raft.consensusModule;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.StateService;
import com.springraft.raft.communication.message.AppendEntries;
import com.springraft.raft.communication.message.AppendEntriesReply;
import com.springraft.raft.communication.message.RequestVote;
import com.springraft.raft.communication.message.RequestVoteReply;
import com.springraft.raft.communication.outbound.OutboundManager;
import com.springraft.raft.config.RaftProperties;
import com.springraft.raft.stateMachine.StateMachineWorker;
import com.springraft.raft.stateMachine.WaitingRequests;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public abstract class RaftStateContext {

    /* Application Context for getting beans */
    protected final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    protected final ConsensusModule consensusModule;

    /* Service to access persisted state repository */
    protected final StateService stateService;

    /* Service to access persisted log repository */
    protected final LogService logService;

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* Timer handles for timeouts */
    protected final TransitionManager transitionManager;

    /* Publisher of messages */
    protected final OutboundManager outboundManager;

    /* Publisher of new commitments to State Machine */
    protected final StateMachineWorker stateMachineWorker;

    /* Map that contains the clients waiting requests */
    protected final WaitingRequests waitingRequests;

    /* --------------------------------------------------- */

    /**
     * Method that prepares a reply to the RequestVoteRPC after checking the log against the RequestVote received.
     *
     * @param requestVote Message sent when invoking a RequestVote RPC.
     * @param reply Object that represents the reply to send that has to be filled.
     * */
    protected void checkLog(RequestVote requestVote, RequestVoteReply reply) {

        LogState logState = this.logService.getState();

        if (requestVote.getLastLogTerm() > logState.getCommittedTerm()) {

            // vote for this request if not voted for anyone yet
            this.setVote(requestVote, reply);

        } else if (requestVote.getLastLogTerm() < logState.getCommittedTerm()) {

            // revoke request
            reply.setVoteGranted(false);

        } else if (requestVote.getLastLogTerm() == (long) logState.getCommittedTerm()) {

            if (requestVote.getLastLogIndex() >= logState.getCommittedIndex()) {

                // vote for this request if not voted for anyone yet
                this.setVote(requestVote, reply);

            } else if (requestVote.getLastLogIndex() < logState.getCommittedIndex()) {

                // revoke request
                reply.setVoteGranted(false);

            }

        }

    }

    /**
     * Method that has replicated code used in checkLog method.
     *
     * @param requestVote Message sent when invoking a RequestVote RPC.
     * @param reply Object that represents the reply to send that has to be filled.
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

    /* --------------------------------------------------- */

    /**
     * Shared method between the 3 raft states for the handling of an AppendEntriesRPC.
     *
     * @param appendEntries Message that contains the information of an AppendEntries request communication.
     *
     * @return AppendEntriesReply Object that represents the reply of that RPC.
     * */
    protected AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        AppendEntriesReply reply = this.applicationContext.getBean(AppendEntriesReply.class);
        reply.setFromIndex(0L);
        reply.setToIndex(0L);

        long currentTerm = this.stateService.getCurrentTerm();

        if (appendEntries.getTerm() < currentTerm) {

            reply.setTerm(currentTerm);
            reply.setSuccess(false);

        } else {

            if (appendEntries.getTerm() > currentTerm) {
                // update term
                this.stateService.setState(appendEntries.getTerm(), null);
            }

            this.setAppendEntriesReply(appendEntries, reply);
            this.postAppendEntries(appendEntries);

        }

        return reply;

    }

    /**
     * A method that encapsulates replicated code, and has the function of setting
     * the reply for the received AppendEntries.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param reply AppendEntriesReply object, to send as response to the leader.
     * */
    private void setAppendEntriesReply(AppendEntries appendEntries, AppendEntriesReply reply) {

        // reply with the current term
        reply.setTerm(appendEntries.getTerm());

        Entry entry = this.logService.getEntryByIndex(appendEntries.getPrevLogIndex());
        entry = entry == null ? (Entry) this.applicationContext.getBean("EntryZero") : entry;

        if((entry.getIndex() == (long) appendEntries.getPrevLogIndex()) && (entry.getTerm() == (long) appendEntries.getPrevLogTerm())) {

            reply.setSuccess(true);
            this.applyAppendEntries(appendEntries, reply);

        } else {

            reply.setSuccess(false);

        }

    }

    /**
     * Method for insert new entries in log and update the committed values in log state.
     *
     * @param appendEntries The received AppendEntries communication.
     * */
    private void applyAppendEntries(AppendEntries appendEntries, AppendEntriesReply reply) {

        int appendEntriesSize = appendEntries.getEntries().size();

        if (appendEntriesSize != 0) {

            // update reply
            reply.setFromIndex(appendEntries.getEntries().get(0).getIndex());
            reply.setToIndex(appendEntries.getEntries().get(appendEntriesSize - 1).getIndex());

            // delete all the following conflict entries
            this.logService.deleteIndexesGreaterThan(appendEntries.getPrevLogIndex());

            // insert entries
            List<? extends Entry> entries = this.logService.saveAllEntries(appendEntries.getEntries());
            entries.sort(Comparator.comparing(Entry::getIndex).reversed());

            // update committed entries in LogState
            this.updateCommittedEntries(appendEntries, entries.get(0));

        } else {

            this.updateCommittedEntries(appendEntries, this.logService.getLastEntry());

        }

    }

    /**
     * Method that updates the log state in case of leaderCommit > committedIndex.
     * It also notifies the state machine worker because of a new commit if that's the case.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param lastEntry The last Entry to compare values.
     * */
    private void updateCommittedEntries (AppendEntries appendEntries, Entry lastEntry) {

        LogState logState = this.logService.getState();
        if (appendEntries.getLeaderCommit() > logState.getCommittedIndex()) {

            Entry entry = lastEntry.getIndex() <= appendEntries.getLeaderCommit()
                    ? lastEntry
                    : this.logService.getEntryByIndex(appendEntries.getLeaderCommit());

            logState.setCommittedIndex(entry.getIndex());
            logState.setCommittedTerm(entry.getTerm());
            this.logService.saveState(logState);

            // notify state machine of a new commit
            this.stateMachineWorker.newCommit();

        }

    }

    /**
     * Abstract method for the Raft state to implement it for post execution operations.
     *
     * @param appendEntries The received AppendEntries communication.
     * */
    protected abstract void postAppendEntries(AppendEntries appendEntries);

    /* --------------------------------------------------- */

}

