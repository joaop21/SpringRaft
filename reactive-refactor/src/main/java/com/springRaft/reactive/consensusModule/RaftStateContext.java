package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.AppendEntriesReply;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.Entry;
import com.springRaft.reactive.persistence.log.LogService;
import com.springRaft.reactive.persistence.log.LogState;
import com.springRaft.reactive.persistence.state.State;
import com.springRaft.reactive.persistence.state.StateService;
import com.springRaft.reactive.util.Pair;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

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
     * Method that prepares a reply to the RequestVoteRPC after checking the log against the RequestVote received.
     *
     * @param requestVote Message sent when invoking a RequestVote RPC.
     * @param reply Object that represents the reply to send that has to be filled.
     *
     * @return RequestVoteReply Reply after checking the RequestVote against the log.
     * */
    protected Mono<RequestVoteReply> checkLog(RequestVote requestVote, RequestVoteReply reply) {

        return this.logService.getState()
                .flatMap(logState -> {

                    if (requestVote.getLastLogTerm() > logState.getCommittedTerm()) {

                        // vote for this request if not voted for anyone yet
                        return this.setVote(requestVote, reply);

                    } else if (requestVote.getLastLogTerm() < logState.getCommittedTerm()) {

                        // revoke request
                        reply.setVoteGranted(false);

                    } else if (requestVote.getLastLogTerm() == (long) logState.getCommittedTerm()) {

                        if (requestVote.getLastLogIndex() >= logState.getCommittedIndex()) {

                            // vote for this request if not voted for anyone yet
                            return this.setVote(requestVote, reply);

                        } else if (requestVote.getLastLogIndex() < logState.getCommittedIndex()) {

                            // revoke request
                            reply.setVoteGranted(false);

                        }

                    }

                    return Mono.just(reply);

                });

    }

    /**
     * Method that has replicated code used in checkLog method.
     *
     * @param requestVote Message sent when invoking a RequestVote RPC.
     * @param reply Object that represents the reply to send that has to be filled.
     *
     * @return RequestVoteReply Reply after checking the RequestVote against the log.
     * */
    private Mono<RequestVoteReply> setVote(RequestVote requestVote, RequestVoteReply reply) {

        return this.stateService.getVotedFor()
                .flatMap(votedFor -> {

                    if (votedFor.equals("") || votedFor.equals(requestVote.getCandidateId())) {

                        return this.stateService.setVotedFor(requestVote.getCandidateId())
                                .map(state -> {
                                    reply.setVoteGranted(true);
                                    return reply;
                                });

                    } else {

                        reply.setVoteGranted(false);
                        return Mono.just(reply);

                    }

                });

    }

    /* --------------------------------------------------- */

    /**
     * Shared method between the 3 raft states for the handling of an AppendEntriesRPC.
     *
     * @param appendEntries Message that contains the information of an AppendEntries request communication.
     *
     * @return AppendEntriesReply Object that represents the reply of that RPC.
     * */
    protected Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> {

                    AppendEntriesReply reply = this.applicationContext.getBean(AppendEntriesReply.class);

                    if (appendEntries.getTerm() < currentTerm) {

                        reply.setTerm(currentTerm);
                        reply.setSuccess(false);
                        return Mono.just(reply);

                    } else {

                        Mono<State> stateMono = Mono.just(new State(null, null, null, true));

                        if (appendEntries.getTerm() > currentTerm) {
                            // update term
                            stateMono = this.stateService.setState(appendEntries.getTerm(), null);
                        }

                        return stateMono.flatMap(state ->
                                this.setAppendEntriesReply(appendEntries, reply)
                                        .flatMap(appendEntriesReply ->
                                                this.postAppendEntries(appendEntries).then(Mono.just(appendEntriesReply)))
                        );

                    }

                });

    }

    /**
     * A method that encapsulates replicated code, and has the function of setting
     * the reply for the received AppendEntries.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param reply AppendEntriesReply object, to send as response to the leader.
     * */
    private Mono<AppendEntriesReply> setAppendEntriesReply(AppendEntries appendEntries, AppendEntriesReply reply) {

        return this.logService.getEntryByIndex(appendEntries.getPrevLogIndex())
                .switchIfEmpty(Mono.just(new Entry((long) 0, (long) 0, null, false)))
                .doFirst(() -> {
                    // reply with the current term
                    reply.setTerm(appendEntries.getTerm());
                })
                .flatMap(entry -> {

                    if((entry.getIndex() == (long) appendEntries.getPrevLogIndex()) && (entry.getTerm() == (long) appendEntries.getPrevLogTerm())) {

                        reply.setSuccess(true);
                        return this.applyAppendEntries(appendEntries).then(Mono.just(reply));

                    } else {

                        reply.setSuccess(false);
                        return Mono.just(reply);

                    }

                });

    }

    /**
     * Method for insert new entries in log and update the committed values in log state.
     *
     * @param appendEntries The received AppendEntries communication.
     * */
    private Mono<Void> applyAppendEntries(AppendEntries appendEntries) {

        if (appendEntries.getEntries().size() != 0) {

            // delete all the following conflict entries
            return this.logService.deleteIndexesGreaterThan(appendEntries.getPrevLogIndex())
                    .thenMany(Flux.fromIterable(appendEntries.getEntries()))
                    .map(entry -> {
                        entry.setNew(true);
                        return entry;
                    })
                    .collectList()
                    .flatMapMany(this.logService::saveAllEntries)
                    .collectSortedList(Comparator.comparing(Entry::getIndex).reversed())
                    .flatMap(entries -> this.updateCommittedEntries(appendEntries, entries.get(0)));

        } else {

            return this.logService.getLastEntry()
                    .flatMap(entry -> this.updateCommittedEntries(appendEntries,entry));

        }

    }

    /**
     * Method that updates the log state in case of leaderCommit > committedIndex.
     * It also notifies the state machine worker because of a new commit if that's the case.
     *
     * @param appendEntries The received AppendEntries communication.
     * @param lastEntry The last Entry to compare values.
     * */
    private Mono<Void> updateCommittedEntries (AppendEntries appendEntries, Entry lastEntry) {

        Mono<Entry> entryMono = Mono.just(lastEntry.getIndex() <= appendEntries.getLeaderCommit())
                .flatMap(conditional ->
                    conditional ? Mono.just(lastEntry) : this.logService.getEntryByIndex(appendEntries.getLeaderCommit())
                );

        return this.logService.getState()
                .filter(logState -> appendEntries.getLeaderCommit() > logState.getCommittedIndex())
                    .zipWith(entryMono, Pair::new)
                    .flatMap(pair -> {

                        LogState logState = pair.first();
                        Entry entry = pair.second();

                        logState.setCommittedIndex(entry.getIndex());
                        logState.setCommittedTerm(entry.getTerm());
                        logState.setNew(false);

                        return this.logService.saveState(logState);

                    })
                .then();

        // ....
        // IT NEEDS TO ADD THIS CODE
        // ....
        // notify state machine of a new commit
        //this.commitmentPublisher.newCommit();

    }

    /**
     * Abstract method for the Raft state to implement it for post execution operations.
     *
     * @param appendEntries The received AppendEntries communication.
     *
     * @return Mono<Void> The result is not important, but it needs to be subscribed.
     * */
    protected abstract Mono<Void> postAppendEntries(AppendEntries appendEntries);

    /* --------------------------------------------------- */

}
