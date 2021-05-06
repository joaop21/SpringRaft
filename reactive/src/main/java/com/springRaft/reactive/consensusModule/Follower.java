package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.LogService;
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
        this.leaderId = raftProperties.getHost();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return super.appendEntries(appendEntries);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> {

                    if (appendEntriesReply.getTerm() > currentTerm) {

                        return this.stateService.setState(appendEntriesReply.getTerm(), null)
                                .doOnTerminate(() -> {

                                    // delete the existing scheduled task
                                    this.scheduledTransition.dispose();

                                    // set a new timeout, it's equivalent to transit to a new follower state
                                    this.setTimeout();

                                })
                                .then();

                    } else {

                        return Mono.empty();

                    }

                });

    }

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

                    if(requestVote.getTerm() < currentTerm) {

                        // revoke request
                        reply.setTerm(currentTerm);
                        reply.setVoteGranted(false);

                    } else if (requestVote.getTerm() > currentTerm) {

                        reply.setTerm(requestVote.getTerm());

                        // update term
                        return this.stateService.setState(requestVote.getTerm(), null)
                                .flatMap(state -> this.checkLog(requestVote, reply))
                                .doOnTerminate(() -> {

                                    // delete the existing scheduled task
                                    this.scheduledTransition.dispose();

                                    // set a new timeout, it's equivalent to transit to a new follower state
                                    this.setTimeout();

                                });

                    } else if (requestVote.getTerm() == currentTerm) {

                        reply.setTerm(currentTerm);

                        return this.checkLog(requestVote, reply);

                    }

                    return Mono.defer(() -> Mono.just(reply));

                });

    }

    @Override
    public Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply) {

        return this.stateService.getCurrentTerm()
                // if term is greater than mine, I should update it and transit to new follower
                .filter(currentTerm -> requestVoteReply.getTerm() > currentTerm)
                .flatMap(currentTerm ->

                    // update term
                    this.stateService.setState(requestVoteReply.getTerm(), null)
                            .doOnTerminate(() -> {

                                // delete the existing scheduled task
                                this.scheduledTransition.dispose();

                                // set a new timeout, it's equivalent to transit to a new follower state
                                this.setTimeout();

                            })
                )
                .then();

    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {

        // When in follower state, we need to redirect the request to the leader
        return Mono.defer(() ->
                Mono.just(
                        this.applicationContext.getBean(
                                RequestReply.class, false,
                                new Object(), true, this.leaderId
                        )
                )
        );

    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {

        return Mono.defer(() -> Mono.just(new Pair<>(null, false)));

    }

    @Override
    public Mono<Void> start() {

        return this.transitionManager.setElectionTimeout()
                .doFirst(() -> {

                    log.info("FOLLOWER");

                    this.leaderId = this.raftProperties.getHost();

                })
                .doOnNext(task -> this.scheduledTransition = task)
                .then();

    }

    /* --------------------------------------------------- */

    @Override
    protected Mono<Void> postAppendEntries(AppendEntries appendEntries) {

        return Mono.defer(() -> {

            this.leaderId = appendEntries.getLeaderId();

            // delete the existing scheduled task
            this.scheduledTransition.dispose();

            // set a new timeout, it's equivalent to transit to a new follower state
            this.setTimeout();

            return Mono.empty();

        });

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

}
