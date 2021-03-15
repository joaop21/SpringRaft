package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.log.Entry;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.persistence.state.State;
import com.springRaft.servlet.persistence.state.StateService;
import com.springRaft.servlet.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            OutboundManager outboundManager
    ) {
        super(
                applicationContext, consensusModule,
                stateService, logService, raftProperties,
                transitionManager, outboundManager
        );

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
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

            this.cleanVolatileState();

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
        // The algorithm ensures that no two leaders exist in the same term,
        // so I cannot receive AppendEntries with the same term as mine when I'm in the Leader state.


        return reply;

    }

    @Override
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply) {

        // Some actions
        // For the leader's election this isn't important
        // Only the heartbeat is needed

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

        }

    }

    @Override
    public Pair<Message, Boolean> getNextMessage(String to) {

        // get next index for specific "to" server
        Long index = this.nextIndex.get(to);
        Entry entry = this.logService.getEntryByIndex(index);

        if (entry == null) {
            // if there is no entry in log then send heartbeat

            return new Pair<>(this.heartbeatAppendEntries(), true);

        } else if (this.matchIndex.get(to) == (index - 1)) {
            // if there is an entry, and the logs are matching,
            // send that entry

            return new Pair<>(this.createAppendEntries(entry, new ArrayList<>(List.of(entry.getCommand()))), false);

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

        // appends the command to its log as a new entry
        Entry entry = this.logService.insertEntry(new Entry(this.stateService.getCurrentTerm(), command));
        log.info("NEW ENTRY IN LOG: " + entry.toString());

        // notify PeerWorkers that a new request is available
        // ...
        this.outboundManager.newMessage();

        // temporary response
        // ...
        // ...
        // ...
        return this.applicationContext.getBean(RequestReply.class, true, false, null);

    }

    /* --------------------------------------------------- */

    @Override
    protected void setAppendEntriesReply(AppendEntries appendEntries, AppendEntriesReply reply) {
        // not needed in Leader
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private void reinitializeVolatileState() {

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

        Long defaultNextIndex = this.logService.getLastEntryIndex() + 1;

        for (InetSocketAddress addr : this.raftProperties.getCluster()) {

            String serverName = this.raftProperties.AddressToString(addr);

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
    private AppendEntries createAppendEntries(Entry entry, List<String> entries) {

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
    private AppendEntries createAppendEntries(State state, LogState logState, Entry lastEntry, List<String> entries) {

        return this.applicationContext.getBean(
                AppendEntries.class,
                state.getCurrentTerm(), // term
                this.raftProperties.AddressToString(this.raftProperties.getHost()), // leaderId
                lastEntry.getIndex(), // prevLogIndex
                lastEntry.getTerm(), // prevLogTerm
                entries, // entries
                logState.getCommittedIndex() // leaderCommit
        );

    }

}
