package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
public class Follower extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Follower.class);

    /* Scheduled Runnable for state transition*/
    private Disposable scheduledTransition;

    /* Leader's ID so requests can be redirected */
    private String leaderId;

    /* --------------------------------------------------- */

    public Follower(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            RaftProperties raftProperties
    ) {
        super(applicationContext, consensusModule,raftProperties);
        this.scheduledTransition = null;
        this.leaderId = raftProperties.getHost();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return null;
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return null;
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return null;
    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return null;
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return null;
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return null;
    }

    @Override
    public Mono<Void> start() {
        return null;
    }

    /* --------------------------------------------------- */
}
