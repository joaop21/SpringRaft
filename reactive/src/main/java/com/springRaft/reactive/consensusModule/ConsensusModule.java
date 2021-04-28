package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
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
    public void setCurrentState(RaftState state) {
        this.current = state;
        this.start().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return this.current.requestVote(requestVote);
    }

    @Override
    @Synchronized
    public void requestVoteReply(RequestVoteReply requestVoteReply) {
        this.current.requestVoteReply(requestVoteReply);
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
