package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.log.Entry;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

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

    /* --------------------------------------------------- */

    /**
     * TODO
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

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    protected AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        AppendEntriesReply reply = this.applicationContext.getBean(AppendEntriesReply.class);

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
        entry = entry == null ? new Entry((long) 0, (long) 0, null) : entry;

        if(entry.getIndex() == (long) appendEntries.getPrevLogIndex()) {

            if (entry.getTerm() == (long) appendEntries.getPrevLogTerm()) {

                reply.setSuccess(true);

                this.applyAppendEntries(appendEntries);

            } else {

                reply.setSuccess(false);

            }

        } else {

            reply.setSuccess(false);

        }

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
            Entry newEntry = new Entry(appendEntries.getEntries().get(0).getTerm(), appendEntries.getEntries().get(0).getCommand());
            newEntry = this.logService.insertEntry(newEntry);

            // update committed entries in LogState
            LogState logState = this.logService.getState();
            if (appendEntries.getLeaderCommit() > logState.getCommittedIndex()) {

                Entry entry = newEntry.getIndex() <= appendEntries.getLeaderCommit()
                        ? newEntry
                        : this.logService.getEntryByIndex(appendEntries.getLeaderCommit());

                logState.setCommittedIndex(entry.getIndex());
                logState.setCommittedTerm(entry.getTerm());
                this.logService.saveState(logState);

            }

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
