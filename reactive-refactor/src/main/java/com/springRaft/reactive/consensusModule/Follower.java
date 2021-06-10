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
        return Mono.empty();
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> {

                    RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);

                    if(requestVote.getTerm() < currentTerm) {

                        // revoke request
                        reply.setTerm(currentTerm);
                        reply.setVoteGranted(false);
                        return Mono.just(reply);

                    } else if (requestVote.getTerm() > currentTerm) {

                        reply.setTerm(requestVote.getTerm());

                        // update term
                        return this.stateService.setState(requestVote.getTerm(), null)
                                .flatMap(state -> this.checkLog(requestVote, reply))
                                .flatMap(requestVoteReply -> this.cleanBeforeTransit().then(Mono.just(requestVoteReply)));

                    } else {

                        reply.setTerm(currentTerm);

                        return this.checkLog(requestVote, reply);

                    }

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
                    )
                    .flatMap(state -> this.cleanBeforeTransit());

    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        // When in follower state, we need to redirect the request to the leader
        return Mono.just(
                this.applicationContext.getBean(RequestReply.class, false, new Object(), true, this.leaderId)
        );
    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return Mono.just(new Pair<>(null, false));
    }

    @Override
    public Mono<Void> start() {

        return this.transitionManager.setElectionTimeout()
                .doFirst(() -> {
                    log.info("FOLLOWER");
                    this.leaderId = this.raftProperties.getHost();
                })
                .doOnNext(task -> this.scheduledTransition = task)
                .then(this.outboundManager.newFollowerState());

    }

    /* --------------------------------------------------- */

    @Override
    protected Mono<Void> postAppendEntries(AppendEntries appendEntries) {
        return this.cleanBeforeTransit().doFirst(() -> this.leaderId = appendEntries.getLeaderId());
    }

    /* --------------------------------------------------- */

    /**
     * Set a timer in milliseconds that represents a timeout.
     * */
    private Mono<Void> setTimeout() {

        return this.transitionManager.setElectionTimeout()
                .doOnNext(task -> this.scheduledTransition = task)
                .then();

    }

    /**
     * Method that cleans the volatile state before set a new timeout.
     * */
    private Mono<Void> cleanBeforeTransit() {

        return Mono.defer(() -> {

            // delete the existing scheduled task
            this.scheduledTransition.dispose();

            // set a new timeout, it's equivalent to transit to a new follower state
            return this.setTimeout();

        });

    }
}
