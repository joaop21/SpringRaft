package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@Scope("singleton")
public class Leader extends RaftStateContext implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Leader.class);

    /* for each server, index of the next log entry to send to that server
        (initialized to leader last log index + 1) */
    private Map<String,Long> nextIndex;

    /* for each server, index of highest log entry known to be replicated on server
        (initialized to 0, increases monotonically) */
    private Map<String,Long> matchIndex;

    /* --------------------------------------------------- */

    public Leader(
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

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
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

                    if (requestVote.getTerm() <= currentTerm) {

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
                                .flatMap(state ->
                                        // check if candidate's log is at least as up-to-date as mine
                                        this.checkLog(requestVote, reply)
                                )
                                .doOnTerminate(() -> {

                                    this.cleanVolatileState();

                                    // transit to follower state
                                    this.transitionManager.setNewFollowerState();

                                    // deactivate PeerWorker
                                    this.outboundManager.clearMessages().subscribe();

                                });

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
                                .doOnTerminate(() -> {

                                    // clean leader's state
                                    this.cleanVolatileState();

                                    // transit to follower state
                                    this.transitionManager.setNewFollowerState();

                                    // deactivate PeerWorker
                                    this.outboundManager.clearMessages().subscribe();

                                })
                )
                .then();

    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {
        return null;
    }

    @Override
    public Mono<Void> start() {

        return this.reinitializeVolatileState()
                .doFirst(() -> log.info("LEADER"))
                .doOnTerminate(() -> {

                    // issue empty AppendEntries in parallel to each of the other servers in the cluster
                    this.outboundManager.newMessage().subscribe();

                });

    }

    /* --------------------------------------------------- */

    /**
     * Method for initialize the volatile variables of leader state.
     *
     * @return Mono<Void> Mono with no important result.
     * */
    private Mono<Void> reinitializeVolatileState() {

        // Long defaultNextIndex = this.logService.getLastEntryIndex() + 1;

        return Flux.fromIterable(this.raftProperties.getCluster())
                .doFirst(() -> {

                    this.nextIndex = new HashMap<>();
                    this.matchIndex = new HashMap<>();

                })
                .doOnNext(serverName -> {

                    // this.nextIndex.put(serverName, defaultNextIndex);

                    this.nextIndex.put(serverName, (long) 0);
                    this.matchIndex.put(serverName, (long) 0);

                })
                .then();

    }

    /**
     * This method cleans the Leader's volatile state.
     * */
    private void cleanVolatileState() {

        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();

    }

}
