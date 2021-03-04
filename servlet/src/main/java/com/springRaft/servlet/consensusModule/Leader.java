package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.persistence.state.State;
import com.springRaft.servlet.persistence.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope("singleton")
public class Leader implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Leader.class);

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* for each server, index of the next log entry to send to that server
        (initialized to leader last log index + 1) */
    private Map<String,Long> nextIndex;

    /* for each server, index of highest log entry known to be replicated on server
        (initialized to 0, increases monotonically) */
    private Map<String,Long> matchIndex;

    /* --------------------------------------------------- */

    public Leader(
            ApplicationContext applicationContext,
            RaftProperties raftProperties,
            StateService stateService,
            OutboundManager outboundManager
    ) {
        this.applicationContext = applicationContext;
        this.raftProperties = raftProperties;
        this.stateService = stateService;
        this.outboundManager = outboundManager;

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
    }

    /* --------------------------------------------------- */

    @Override
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        // Some actions

        return null;

    }

    @Override
    public void appendEntriesReply(AppendEntriesReply appendEntriesReply) {

        // Some actions

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);
        reply.setTerm(this.stateService.getCurrentTerm());
        reply.setVoteGranted(false);

        return reply;
    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

    }

    @Override
    public Message getNextMessage(String to) {

        return this.emptyAppendEntries();

    }

    @Override
    public void start() {

        log.info("LEADER");

        this.reinitializeVolatileState();

        // issue empty AppendEntries in parallel to each of the other servers in the cluster
        this.outboundManager.newMessage();

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private void reinitializeVolatileState() {

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

        for (InetSocketAddress addr : this.raftProperties.getCluster()) {

            String serverName = this.raftProperties.AddressToString(addr);

            // next index must be changed to the persisted value + 1
            // ...
            // ...
            this.nextIndex.put(serverName, (long) 1);
            this.matchIndex.put(serverName, (long) 0);

        }

    }

    private AppendEntries emptyAppendEntries() {

        State state = this.stateService.getState();

        // this need to change to correct values
        // ...
        // ...
        return this.applicationContext.getBean(
                AppendEntries.class,
                state.getCurrentTerm(), // term
                this.raftProperties.AddressToString(this.raftProperties.getHost()), // leaderId
                0, // prevLogIndex
                0, // prevLogTerm
                new ArrayList<>(), // entries
                0 // leaderCommit
                );

    }

}
