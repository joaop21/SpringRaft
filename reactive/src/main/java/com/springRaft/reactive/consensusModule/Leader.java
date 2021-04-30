package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.Entry;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return null;
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {
        return null;
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

        return Mono.defer(() ->
                Mono.just(new Pair<>(this.nextIndex.get(to), this.matchIndex.get(to)))
        )
                .flatMap(pair -> {

                    // get next index for specific "to" server
                    Long nextIndex = pair.getFirst();
                    Long matchIndex = pair.getSecond();

                    return this.logService.getEntryByIndex(nextIndex)
                            .switchIfEmpty(
                                    Mono.defer(() ->
                                        Mono.just(new Entry(null, null, null, false))
                                    )
                            )
                            .flatMap(entry -> {

                                if (entry.getIndex() == null && matchIndex == (nextIndex - 1)) {
                                    // if there is no entry in log then send heartbeat

                                    return this.heartbeatAppendEntries()
                                            .map(message -> new Pair<>(message, true));

                                } else if(entry.getIndex() == null) {
                                    // if there is no entry and the logs are not matching

                                    return this.heartbeatAppendEntries()
                                            .map(message -> new Pair<>(message, false));

                                } else {

                                    return this.heartbeatAppendEntries()
                                            .map(message -> new Pair<>(message, true));

                                }

                            });

                });

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

                    this.nextIndex.put(serverName, (long) 1);
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

    /**
     * Method that creates an AppendEntries with no entries that represents an heartbeat.
     *
     * @return AppendEntries Message to pass to an up-to-date follower.
     * */
    private Mono<AppendEntries> heartbeatAppendEntries() {

        Mono<State> stateMono = this.stateService.getState();
        Mono<LogState> logStateMono = this.logService.getState();
        Mono<Entry> lastEntryMono = this.logService.getLastEntry();

        return Mono.zip(stateMono, logStateMono, lastEntryMono)
                .flatMap(tuple ->
                        this.createAppendEntries(tuple.getT1(), tuple.getT2(), tuple.getT3(), new ArrayList<>())
                );

    }

    /**
     * Method that creates an AppendEntries with the new entry.
     *
     * @param state State for getting current term.
     * @param logState Log state for getting the committed index.
     * @param lastEntry Last Entry in the log for getting its index and term.
     * @param entries Entries to send in the AppendEntries.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private Mono<AppendEntries> createAppendEntries(State state, LogState logState, Entry lastEntry, List<Entry> entries) {

        return Mono.defer(() ->
                Mono.just(
                    this.applicationContext.getBean(
                        AppendEntries.class,
                        state.getCurrentTerm(), // term
                        this.raftProperties.getHost(), // leaderId
                        lastEntry.getIndex(), // prevLogIndex
                        lastEntry.getTerm(), // prevLogTerm
                        entries, // entries
                        logState.getCommittedIndex() // leaderCommit
                    )
                )
        );

    }

}
