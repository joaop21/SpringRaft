package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.log.Entry;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.persistence.state.State;
import com.springRaft.servlet.persistence.state.StateService;
import com.springRaft.servlet.stateMachine.CommitmentPublisher;
import com.springRaft.servlet.stateMachine.WaitingRequests;
import com.springRaft.servlet.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("singleton")
public class Leader extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Leader.class);

    /* for each server, index of the next log entry to send to that server
        (initialized to leader last log index + 1) */
    private Map<String,Long> nextIndex;

    /* for each server, index of highest log entry known to be replicated on server
        (initialized to 0, increases monotonically) */
    private Map<String,Long> matchIndex;

    /* --------------------------------------------------- */

    public Leader(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            LogService logService,
            RaftProperties raftProperties,
            TransitionManager transitionManager,
            OutboundManager outboundManager,
            CommitmentPublisher commitmentPublisher,
            WaitingRequests waitingRequests
    ) {
        super(
                applicationContext, consensusModule,
                stateService, logService, raftProperties,
                transitionManager, outboundManager,
                commitmentPublisher, waitingRequests
        );

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
    }

    /* --------------------------------------------------- */

    @Override
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        return super.appendEntries(appendEntries);

    }

    @Override
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {

        if (appendEntriesReply.getSuccess()) {

            long nextIndex = this.nextIndex.get(from);
            long matchIndex = this.matchIndex.get(from);

            // compare last entry with next index for "from" server
            if (nextIndex != this.logService.getLastEntryIndex() + 1) {

                if (matchIndex != (nextIndex - 1)) {

                    this.matchIndex.put(from, nextIndex - 1);

                } else {

                    this.matchIndex.put(from, nextIndex);
                    this.nextIndex.put(from, nextIndex + 1);

                }

            } else {

                this.matchIndex.put(from, nextIndex - 1);

            }

            // check if it is needed to set committed index
            this.setCommitIndex(from);

        } else {

            // if term is greater than mine, I should update it and transit to new follower
            if (appendEntriesReply.getTerm() > this.stateService.getCurrentTerm()) {

                // update term
                this.stateService.setState(appendEntriesReply.getTerm(), null);

                // clean leader's state
                this.cleanVolatileState();

                // transit to follower state
                this.transitionManager.setNewFollowerState();

                // deactivate PeerWorker
                this.outboundManager.clearMessages();

            } else {

                this.nextIndex.put(from, this.nextIndex.get(from) - 1);
                this.matchIndex.put(from, (long) 0);

            }

        }

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

            this.cleanVolatileState();

            // transit to follower state
            this.transitionManager.setNewFollowerState();

            // deactivate PeerWorker
            this.outboundManager.clearMessages();

        }

        return reply;
    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

        // if term is greater than mine, I should update it and transit to new follower
        if (requestVoteReply.getTerm() > this.stateService.getCurrentTerm()) {

            // update term
            this.stateService.setState(requestVoteReply.getTerm(), null);

            // clean leader's state
            this.cleanVolatileState();

            // transit to follower state
            this.transitionManager.setNewFollowerState();

            // deactivate PeerWorker
            this.outboundManager.clearMessages();

        }

    }

    @Override
    public Pair<Message, Boolean> getNextMessage(String to) {

        // get next index for specific "to" server
        Long nextIndex = this.nextIndex.get(to);
        Long matchIndex = this.matchIndex.get(to);
        Entry entry = this.logService.getEntryByIndex(nextIndex);

        if (entry == null && matchIndex == (nextIndex - 1)) {
            // if there is no entry in log then send heartbeat

            return new Pair<>(this.heartbeatAppendEntries(), true);

        } else if (entry == null) {
            // if there is no entry and the logs are not matching

            return new Pair<>(this.heartbeatAppendEntries(), false);

        } else if (matchIndex == (nextIndex - 1)) {
            // if there is an entry, and the logs are matching,
            // send that entry

            List<Entry> entries = this.logService.getEntryBetweenIndex(nextIndex, nextIndex + this.raftProperties.getEntriesPerCommunication());
            entries.sort(Comparator.comparing(Entry::getIndex));

            this.nextIndex.put(to, nextIndex + entries.size());

            return new Pair<>(this.createAppendEntries(entries.get(0), entries), false);

        } else {
            // if there is an entry, but the logs are not matching,
            // send the appendEntries with no entries

            return new Pair<>(this.createAppendEntries(entry), false);

        }

    }

    @Override
    public void start() {

        log.info("LEADER");

        this.reinitializeVolatileState();

        // issue empty AppendEntries in parallel to each of the other servers in the cluster
        this.outboundManager.newMessage();

    }

    @Override
    public RequestReply clientRequest(String command) {

        Entry entry;
        try {
            // appends the command to its log as a new entry
            entry = this.logService.insertEntry(new Entry(this.stateService.getCurrentTerm(), command));
        } catch (Exception exception) {
            return clientRequest(command);
        }

        // notify PeerWorkers that a new request is available
        this.outboundManager.newMessage();

        // get response after state machine applied it
        Object response = this.waitingRequests
                .insertWaitingRequest(entry.getIndex())
                .getResponse();

        return response != null
                ? this.applicationContext.getBean(RequestReply.class, true, response, false, "")
                : this.applicationContext.getBean(RequestReply.class, false, new Object(), false, "");

    }

    /* --------------------------------------------------- */

    @Override
    protected void postAppendEntries(AppendEntries appendEntries) {

        this.cleanVolatileState();

        // transit to follower state
        this.transitionManager.setNewFollowerState();

        // deactivate PeerWorker
        this.outboundManager.clearMessages();

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private void reinitializeVolatileState() {

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

        Long defaultNextIndex = this.logService.getLastEntryIndex() + 1;

        for (String serverName : this.raftProperties.getCluster()) {

            this.nextIndex.put(serverName, defaultNextIndex);
            this.matchIndex.put(serverName, (long) 0);

        }

    }

    /**
     * This method cleans the Leader's volatile state.
     * */
    private void cleanVolatileState() {

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

    }

    /**
     * Method that creates an AppendEntries with no entries that represents an heartbeat.
     *
     * @return AppendEntries Message to pass to an up-to-date follower.
     * */
    private AppendEntries heartbeatAppendEntries() {

        State state = this.stateService.getState();
        LogState logState = this.logService.getState();
        Entry lastEntry = this.logService.getLastEntry();

        return this.createAppendEntries(state, logState, lastEntry, new ArrayList<>());

    }

    /**
     * Method that creates an AppendEntries with the new entry
     *
     * @param entry New entry to replicate.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private AppendEntries createAppendEntries(Entry entry) {

        return this.createAppendEntries(entry, new ArrayList<>());

    }

    /**
     * Method that creates an AppendEntries with the new entry
     *
     * @param entry New entry to replicate.
     * @param entries Entries to send in the AppendEntries.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private AppendEntries createAppendEntries(Entry entry, List<Entry> entries) {

        State state = this.stateService.getState();
        LogState logState = this.logService.getState();
        Entry lastEntry = this.logService.getEntryByIndex(entry.getIndex() - 1);
        lastEntry = lastEntry == null ? new Entry((long) 0, (long) 0, null) : lastEntry;

        return this.createAppendEntries(state, logState, lastEntry, entries);

    }

    /**
     * Method that creates an AppendEntries with the new entry.
     *
     * @param state State for getting current term.
     * @param logState Log state for getting the committed index.
     * @param lastEntry Last Entry in the log for getting its index and term.
     * @param entries Entries to send in the AppendEntries.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private AppendEntries createAppendEntries(State state, LogState logState, Entry lastEntry, List<Entry> entries) {

        return this.applicationContext.getBean(
                AppendEntries.class,
                state.getCurrentTerm(), // term
                this.raftProperties.getHost(), // leaderId
                lastEntry.getIndex(), // prevLogIndex
                lastEntry.getTerm(), // prevLogTerm
                entries, // entries
                logState.getCommittedIndex() // leaderCommit
        );

    }

    /**
     * Method that checks whether it is possible to commit an entry,
     * and if so, commits it in the Log State as well as notifies the StateMachineWorker.
     *
     * @param from String that identifies the server that set a new matchIndex, to fetch its value.
     * */
    private void setCommitIndex(String from) {

        LogState logState = this.logService.getState();
        long N = this.matchIndex.get(from);
        Entry entry = this.logService.getEntryByIndex(N);

        try {
            if (
                    N > logState.getCommittedIndex() &&
                    entry.getTerm() == (long) this.stateService.getCurrentTerm() &&
                    this.majorityOfMatchIndexGreaterOrEqualThan(N)
            ) {

                logState.setCommittedIndex(N);
                logState.setCommittedTerm(entry.getTerm());
                this.logService.saveState(logState);

                // notify state machine of a new commit
                this.commitmentPublisher.newCommit();

            }
        } catch (NullPointerException e) {

            log.info("\n\nEntry: " + entry + "\nIndex: " + N + "\n");

        }



    }

    /**
     * Method that checks if an index is replicated in the majority of the servers
     * in the cluster.
     *
     * @param N Long that represents the index to check.
     *
     * @return boolean that tells if the majority has or has not that index replicated.
     * */
    private boolean majorityOfMatchIndexGreaterOrEqualThan (long N) {

        int count = 1; // 1 because of the leader that is not in matchIndex Map

        for (long index : this.matchIndex.values())
            if (index >= N)
                count++;

        return count >= this.raftProperties.getQuorum();

    }

}
