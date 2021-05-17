package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.startup.PeerWorkers;
import com.springRaft.reactive.util.Pair;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope("singleton")
@Getter
public class ConsensusModule implements RaftState {

    /* Current Raft state - Follower, Candidate, Leader */
    private RaftState current;

    /* Mutex for some operations */
    private final Lock lock;

    /* --------------------------------------------------- */

    public ConsensusModule() {
        this.current = null;
        this.lock = new ReentrantLock();
    }

    /* --------------------------------------------------- */

    /**
     * Setter method which sets the new state in consensus module.
     *
     * @param state The new raft state of this consensus module.
     * */
    public void setCurrentState(RaftState state) {
        this.current = state;
    }

    /**
     * Setter method which sets the new state in consensus module and starts that state.
     *
     * @param state The new raft state of this consensus module.
     * */
    public Mono<Void> setAndStartNewState(RaftState state) {
        this.setCurrentState(state);
        return this.start();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.appendEntries(appendEntries)
                    .flatMap(reply -> {
                        lock.unlock();
                        return Mono.just(reply);
                    });
        });
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.appendEntriesReply(appendEntriesReply, from)
                    .publishOn(PeerWorkers.getPeerWorkerScheduler(from))
                    .then(Mono.create(monoSink -> {
                        lock.unlock();
                        monoSink.success();
                    }));
        });
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.requestVote(requestVote)
                    .flatMap(reply ->
                        Mono.create(monoSink -> {
                            lock.unlock();
                            monoSink.success(reply);
                        })
                    );
        });
    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.requestVoteReply(requestVoteReply)
                    .then(Mono.create(monoSink -> {
                        lock.unlock();
                        monoSink.success();
                    }));
        });
    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.current.clientRequest(command);
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.getNextMessage(to)
                    .publishOn(PeerWorkers.getPeerWorkerScheduler(to))
                    .flatMap(pair -> {
                        lock.unlock();
                        return Mono.just(pair);
                    });
        })
                .subscribeOn(PeerWorkers.getPeerWorkerScheduler(to));
    }

    @Override
    public Mono<Void> start() {
        return Mono.defer(() -> {
            lock.lock();
            return this.current.start()
                    .then(Mono.create(monoSink -> {
                        lock.unlock();
                        monoSink.success();
                    }));
        });
    }

}
