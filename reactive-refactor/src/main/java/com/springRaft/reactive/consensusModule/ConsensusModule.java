package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return this.current.appendEntries(appendEntries);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return this.current.appendEntriesReply(appendEntriesReply, from);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return this.current.requestVote(requestVote);
    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return this.current.requestVoteReply(requestVoteReply);
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return this.current.getNextMessage(to);
    }

    @Override
    public Mono<Void> start() {
        return this.current.start();
    }

    /* --------------------------------------------------- */


}
