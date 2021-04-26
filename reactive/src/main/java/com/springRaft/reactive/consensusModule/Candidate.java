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
import reactor.util.function.Tuple2;

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

                        // revoke request
                        reply.setTerm(currentTerm);
                        reply.setVoteGranted(false);

                    } else if (requestVote.getTerm() > currentTerm) {

                        reply.setTerm(requestVote.getTerm());

                        // update term
                        Mono<State> stateMono = this.stateService.setState(requestVote.getTerm(), null);

                        // check if candidate's log is at least as up-to-date as mine
                        Mono<RequestVoteReply> requestVoteReplyMonoMono = this.checkLog(requestVote, reply);

                        // transit to follower state
                        return Mono.zip(stateMono, requestVoteReplyMonoMono)
                                .map(Tuple2::getT2)
                                .doOnTerminate(() -> {

                                    this.cleanBeforeTransit();

                                    // transit to follower state
                                    this.transitionManager.setNewFollowerState();

                                });

                    }

                    return Mono.just(reply);

                });

    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {

        return Mono.defer(() -> Mono.just(new Pair<>(this.requestVoteMessage, false)));

    }

    @Override
    public void start() {

        log.info("CANDIDATE");

        // set votes granted to none
        this.votesGranted = 0;

        // persist new state
        Mono<State> newStateMono = this.stateService.newCandidateState();
        Mono<LogState> logStateMono = this.logService.getState();

        Mono.zip(newStateMono, logStateMono)
                .doOnNext(tuple -> {

                    State state = tuple.getT1();
                    LogState logState = tuple.getT2();

                    log.info(state.toString());
                    log.info(logState.toString());

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
                    this.outboundManager.newMessage();

                })
                .doAfterTerminate(this::setTimeout)
                .subscribe();

    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private void setTimeout() {

        this.scheduledTransition = this.transitionManager.setElectionTimeout().subscribe();

    }

    /**
     * Clean volatile candidate state before transit to another state.
     * */
    private void cleanBeforeTransit() {

        // delete the existing scheduled task
        this.scheduledTransition.dispose();

        // change message to null and notify peer workers
        this.requestVoteMessage = null;
        this.outboundManager.clearMessages();

    }

}
