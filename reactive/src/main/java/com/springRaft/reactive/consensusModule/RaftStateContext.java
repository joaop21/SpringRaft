package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.AppendEntriesReply;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.Entry;
import com.springRaft.reactive.persistence.log.LogService;
import com.springRaft.reactive.persistence.state.State;
import com.springRaft.reactive.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public abstract class RaftStateContext {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RaftStateContext.class);



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

    /* Publisher of new commitments to State Machine */
    // ...

    /* Map that contains the clients waiting requests */
    // ...

    /* --------------------------------------------------- */

    /**
     * TODO
     *
     * @return
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

                    return Mono.defer(() -> Mono.just(reply));

                });

    }

    /**
     * TODO
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
                        return Mono.defer(() -> Mono.just(reply));

                    }

                });

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    protected Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {

        Mono<AppendEntriesReply> replyMono = Mono.defer(
                () -> Mono.just(this.applicationContext.getBean(AppendEntriesReply.class))
        );

        Mono<Long> currentTermMono = this.stateService.getCurrentTerm();

        return Mono.zip(replyMono, currentTermMono)
                .flatMap(tuple -> {

                    AppendEntriesReply reply = tuple.getT1();
                    long currentTerm = tuple.getT2();

                    if (appendEntries.getTerm() < currentTerm) {

                        reply.setTerm(currentTerm);
                        reply.setSuccess(false);

                        return Mono.defer(() -> Mono.just(reply));

                    } else {

                        Mono<State> stateMono = Mono.just(new State(null, null, null, true));

                        if (appendEntries.getTerm() > currentTerm) {
                            // update term
                            stateMono = this.stateService.setState(appendEntries.getTerm(), null);
                        }

                        return stateMono.flatMap(state ->
                            this.setAppendEntriesReply(appendEntries, reply)
                                    .doOnTerminate(() -> this.postAppendEntries(appendEntries).subscribe())
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

                    if(entry.getIndex() == (long) appendEntries.getPrevLogIndex()) {

                        if (entry.getTerm() == (long) appendEntries.getPrevLogTerm()) {

                            reply.setSuccess(true);

                            // this.applyAppendEntries(appendEntries);

                        } else {

                            reply.setSuccess(false);

                        }

                    } else {

                        reply.setSuccess(false);

                    }

                    return Mono.defer(() -> Mono.just(reply));

                });

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
