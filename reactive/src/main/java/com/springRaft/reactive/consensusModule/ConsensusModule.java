package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.util.Pair;
import lombok.Getter;
import lombok.Synchronized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@Getter
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* --------------------------------------------------- */

    public ConsensusModule() {
        this.current = null;
    }

    /* --------------------------------------------------- */

    /**
     * Setter method which sets the new state in consensus module and starts that state.
     *
     * @param state The new raft state of this consensus module.
     * */
    public Mono<Void> setCurrentState(RaftState state) {
        this.current = state;
        return this.start();
    }

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return this.current.appendEntries(appendEntries);
    }

    @Override
    @Synchronized
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return this.current.appendEntriesReply(appendEntriesReply, from);
    }

    @Override
    @Synchronized
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return this.current.requestVote(requestVote);
    }

    @Override
    @Synchronized
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return this.current.requestVoteReply(requestVoteReply);
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    @Synchronized
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return this.current.getNextMessage(to);
    }

    @Override
    @Synchronized
    public Mono<Void> start() {
        return this.current.start();
    }

}
