package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.LogService;
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

}
