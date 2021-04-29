package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.LogService;
import com.springRaft.reactive.persistence.log.LogState;
import com.springRaft.reactive.persistence.state.State;
import com.springRaft.reactive.persistence.state.StateService;
import com.springRaft.reactive.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Scope("singleton")
public class Candidate extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Scheduled Runnable for state transition*/
    private Disposable scheduledTransition;

    /* Message to send to the cluster requesting votes */
    private Message requestVoteMessage;

    /* Votes granted by the cluster */
    private int votesGranted;

    /* --------------------------------------------------- */

    public Candidate(
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
        this.scheduledTransition = null;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {

        // get a reply object
        Mono<RequestVoteReply> replyMono = Mono.defer(
                () -> Mono.just(this.applicationContext.getBean(RequestVoteReply.class))
        );

        // get the current term
        Mono<Long> currentTermMono = this.stateService.getCurrentTerm();

        return Mono.zip(replyMono, currentTermMono)
                .flatMap(tuple -> {

                    RequestVoteReply reply = tuple.getT1();
                    long currentTerm = tuple.getT2();

                    if(requestVote.getTerm() <= currentTerm) {

                        return Mono.defer(() -> {

                            // revoke request
                            reply.setTerm(currentTerm);
                            reply.setVoteGranted(false);

                            return Mono.just(reply);
                        });

                    } else {

                        reply.setTerm(requestVote.getTerm());

                        // update term
                        return this.stateService.setState(requestVote.getTerm(), null)
                                .flatMap(state -> this.checkLog(requestVote, reply))
                                .doOnTerminate(() -> {

                                    this.cleanBeforeTransit().subscribe();

                                    // transit to follower state
                                    this.transitionManager.setNewFollowerState();

                                });


                    }

                });

    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> {

                    if (requestVoteReply.getTerm() > currentTerm) {

                        // update term
                        return this.stateService.setState(requestVoteReply.getTerm(), null)
                                .doOnTerminate(() -> {

                                    this.cleanBeforeTransit().subscribe();

                                    // transit to follower state
                                    this.transitionManager.setNewFollowerState();

                                });

                    } else {

                        if (requestVoteReply.getVoteGranted()) {

                            this.votesGranted++;

                            if (this.votesGranted >= this.raftProperties.getQuorum()) {

                                // transit to leader state
                                return this.cleanBeforeTransit()
                                        .doOnTerminate(
                                                // transit to leader state
                                                this.transitionManager::setNewLeaderState
                                        );

                            }

                        }

                        return Mono.empty();

                    }

                })
                .then();

    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {

        return Mono.defer(() -> Mono.just(new Pair<>(this.requestVoteMessage, false)));

    }

    @Override
    public Mono<Void> start() {

        // persist new state
        Mono<State> newStateMono = this.stateService.newCandidateState();
        Mono<LogState> logStateMono = this.logService.getState();

        return Mono.zip(newStateMono, logStateMono)
                .doFirst(() -> {

                    log.info("CANDIDATE");

                    // set votes granted to none
                    this.votesGranted = 0;

                })
                .flatMap(tuple -> {

                    State state = tuple.getT1();
                    LogState logState = tuple.getT2();

                    log.info(state.toString());

                    this.votesGranted++;

                    this.requestVoteMessage =
                            this.applicationContext.getBean(
                                    RequestVote.class,
                                    state.getCurrentTerm(),
                                    this.raftProperties.getHost(),
                                    logState.getCommittedIndex(),
                                    logState.getCommittedTerm()
                            );

                    // issue RequestVote RPCs in parallel to each of the other servers in the cluster
                    return this.outboundManager.newMessage();

                })
                .doOnTerminate(this::setTimeout);

    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        this.transitionManager.setElectionTimeout()
                .doOnNext(task -> this.scheduledTransition = task)
                .subscribeOn(Schedulers.single())
                .subscribe();

    }

    /**
     * Clean volatile candidate state before transit to another state.
     *
     * @return Mono<Void> The result it's not important, but something is returned so it can be subscribed.
     * */
    private Mono<Void> cleanBeforeTransit() {

        return Mono.defer(() -> {

            // delete the existing scheduled task
            this.scheduledTransition.dispose();

            // change message to null and notify peer workers
            this.requestVoteMessage = null;
            return this.outboundManager.clearMessages();

        });

    }

}
