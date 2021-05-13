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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

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
        return super.appendEntries(appendEntries);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {

        if (appendEntriesReply.getSuccess()) {

            return this.logService.getLastEntryIndex()
                    .map(index -> index + 1)
                    .doOnNext(lastEntryIndex -> {

                        long nextIndex = this.nextIndex.get(from);
                        long matchIndex = this.matchIndex.get(from);

                        // compare last entry with next index for "from" server
                        if (nextIndex != lastEntryIndex) {

                            if (matchIndex != (nextIndex - 1)) {

                                this.matchIndex.put(from, nextIndex - 1);

                            } else {

                                this.matchIndex.put(from, nextIndex);
                                this.nextIndex.put(from, nextIndex + 1);

                            }

                        } else {

                            this.matchIndex.put(from, nextIndex - 1);

                        }

                    })
                    .flatMap(index -> this.setCommitIndex(from));

        } else {

            return this.stateService.getCurrentTerm()
                    .flatMap(currentTerm -> {

                        // if term is greater than mine, I should update it and transit to new follower
                        if (appendEntriesReply.getTerm() > currentTerm) {

                            return this.stateService.setState(appendEntriesReply.getTerm(), null)
                                    .doOnNext(state ->
                                            // clean leader's state
                                            this.cleanVolatileState()
                                    )
                                    // transit to follower state and
                                    // deactivate PeerWorker
                                    .then(this.transitionManager.setNewFollowerState())
                                    .then(this.outboundManager.clearMessages());

                        } else {

                            this.nextIndex.put(from, this.nextIndex.get(from) - 1);
                            this.matchIndex.put(from, (long) 0);

                            return Mono.empty();

                        }

                    });

        }

    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {

        // get a reply object
        Mono<RequestVoteReply> replyMono = Mono.just(this.applicationContext.getBean(RequestVoteReply.class));
        // get the current term
        Mono<Long> currentTermMono = this.stateService.getCurrentTerm();

        return Mono.zip(replyMono, currentTermMono)
                .flatMap(tuple -> {

                    RequestVoteReply reply = tuple.getT1();
                    long currentTerm = tuple.getT2();

                    if (requestVote.getTerm() <= currentTerm) {

                        // revoke request
                        reply.setTerm(currentTerm);
                        reply.setVoteGranted(false);

                        return Mono.just(reply);

                    } else {

                        reply.setTerm(requestVote.getTerm());

                        // update term
                        return this.stateService.setState(requestVote.getTerm(), null)
                                .flatMap(state ->
                                        // check if candidate's log is at least as up-to-date as mine
                                        this.checkLog(requestVote, reply)
                                )
                                .doOnNext(requestVoteReply -> {
                                    this.cleanVolatileState();
                                })
                                .flatMap(requestVoteReply ->
                                        this.transitionManager.setNewFollowerState()
                                            .then(this.outboundManager.clearMessages())
                                            .then(Mono.just(requestVoteReply))
                                );

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
                        // clean leader's state
                        this.stateService.setState(requestVoteReply.getTerm(), null)
                                .doOnTerminate(this::cleanVolatileState)
                )
                .then(this.transitionManager.setNewFollowerState())
                .then(this.outboundManager.clearMessages());

    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {

        // For now it remains like this
        // but it has to change in order to replicate the request
        // ...

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> this.logService.insertEntry(new Entry(currentTerm, command, true)))
                .flatMap(entry -> Mono.just(
                        this.applicationContext.getBean(
                                RequestReply.class, true,
                                new ResponseEntity<>(entry, HttpStatus.OK), false, ""
                        )
                ))
                .flatMap(requestReply ->
                        this.outboundManager.newMessage()
                            .then(Mono.just(requestReply))
                );

    }

    @Override
    public Mono<Pair<Message, Boolean>> getNextMessage(String to) {

        return Mono.just(new Pair<>(this.nextIndex.get(to), this.matchIndex.get(to)))
                .flatMap(pair -> {

                    // get next index for specific "to" server
                    Long nextIndex = pair.getFirst();
                    Long matchIndex = pair.getSecond();

                    return this.logService.getEntryByIndex(nextIndex)
                            .switchIfEmpty(Mono.just(new Entry(null, null, null, false)))
                            .flatMap(entry -> {

                                if (entry.getIndex() == null && matchIndex == (nextIndex - 1)) {
                                    // if there is no entry in log then send heartbeat

                                    return this.heartbeatAppendEntries()
                                            .map(message -> new Pair<>(message, true));

                                } else if(entry.getIndex() == null) {
                                    // if there is no entry and the logs are not matching

                                    return this.heartbeatAppendEntries()
                                            .map(message -> new Pair<>(message, false));

                                } else if(matchIndex == (nextIndex - 1)) {
                                    // if there is an entry, and the logs are matching,
                                    // send that entry

                                    return this.logService.getEntriesBetweenIndexes(
                                            nextIndex,
                                            nextIndex + this.raftProperties.getEntriesPerCommunication()
                                    )
                                            .collectSortedList(Comparator.comparing(Entry::getIndex))
                                            .doOnNext(entries -> this.nextIndex.put(to, nextIndex + entries.size()))
                                            .flatMap(entries ->
                                                    this.createAppendEntries(entries.get(0), entries)
                                                            .map(message -> new Pair<>(message, false))
                                            );

                                } else {
                                    // if there is an entry, but the logs are not matching,
                                    // send the appendEntries with no entries

                                    return this.createAppendEntries(entry)
                                            .map(message -> new Pair<>(message, true));

                                }

                            });

                });

    }

    @Override
    public Mono<Void> start() {

        return this.reinitializeVolatileState()
                .then(
                        // issue empty AppendEntries in parallel to each of the other servers in the cluster
                        this.outboundManager.newMessage()
                )
                .doFirst(() -> log.info("LEADER"));

    }

    /* --------------------------------------------------- */

    @Override
    protected Mono<Void> postAppendEntries(AppendEntries appendEntries) {

        // transit to follower state
        // deactivate PeerWorker
        return this.transitionManager.setNewFollowerState()
                .then(this.outboundManager.clearMessages())
                .doFirst(this::cleanVolatileState);


    }

    /* --------------------------------------------------- */

    /**
     * Method for initialize the volatile variables of leader state.
     *
     * @return Mono<Void> Mono with no important result.
     * */
    private Mono<Void> reinitializeVolatileState() {

        return this.logService.getLastEntryIndex()
                .map(defaultNextIndex -> defaultNextIndex + 1)
                .flatMap(defaultNextIndex ->

                    Flux.fromIterable(this.raftProperties.getCluster())
                            .doFirst(() -> {
                                this.nextIndex = new HashMap<>();
                                this.matchIndex = new HashMap<>();
                            })
                            .doOnNext(serverName -> {
                                this.nextIndex.put(serverName, defaultNextIndex);
                                this.matchIndex.put(serverName, (long) 0);
                            })
                            .then()

                );

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
     * Method that creates an AppendEntries with the new entry
     *
     * @param entry New entry to replicate.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private Mono<AppendEntries> createAppendEntries(Entry entry) {

        return this.createAppendEntries(entry, new ArrayList<>());

    }

    /**
     * Method that creates an AppendEntries with the new entry
     *
     * @param entry New entry to replicate.
     * @param entries Entries to send in the AppendEntries.
     *
     * @return AppendEntries Message to pass to another server.
     * */
    private Mono<AppendEntries> createAppendEntries(Entry entry, List<Entry> entries) {

        Mono<State> stateMono = this.stateService.getState();
        Mono<LogState> logStateMono = this.logService.getState();
        Mono<Entry> lastEntryMono = this.logService.getEntryByIndex(entry.getIndex() - 1)
                .switchIfEmpty(Mono.just(new Entry((long) 0, (long) 0, null, false)));

        return Mono.zip(stateMono, logStateMono, lastEntryMono)
                .flatMap(tuple3 -> this.createAppendEntries(tuple3.getT1(), tuple3.getT2(), tuple3.getT3(), entries));

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

        return Mono.just(
                    this.applicationContext.getBean(
                        AppendEntries.class,
                        state.getCurrentTerm(), // term
                        this.raftProperties.getHost(), // leaderId
                        lastEntry.getIndex(), // prevLogIndex
                        lastEntry.getTerm(), // prevLogTerm
                        entries, // entries
                        logState.getCommittedIndex() // leaderCommit
                    )
                );

    }

    /**
     * Method that checks whether it is possible to commit an entry,
     * and if so, commits it in the Log State as well as notifies the StateMachineWorker.
     *
     * @param from String that identifies the server that set a new matchIndex, to fetch its value.
     * */
    private Mono<Void> setCommitIndex(String from) {

        long N = this.matchIndex.get(from);

        return Mono.zip(
                    this.logService.getState(),
                    this.logService.getEntryByIndex(N),
                    this.stateService.getCurrentTerm(),
                    this.majorityOfMatchIndexGreaterOrEqualThan(N)
                )
                .filter(tuple ->
                        N > tuple.getT1().getCommittedIndex() &&
                        tuple.getT2().getTerm() == (long) tuple.getT3() &&
                        tuple.getT4()
                )
                .flatMap(tuple -> {

                    LogState logState = tuple.getT1();
                    Entry entry = tuple.getT2();

                    logState.setCommittedIndex(N);
                    logState.setCommittedTerm(entry.getTerm());
                    logState.setNew(false);

                    return this.logService.saveState(logState);
                            // ...
                            // NEEDS MORE CODE HERE
                            // ...
                            //.then(
                                    // notify state machine of a new commit
                              //      this.commitmentPublisher.newCommit()
                            //)


                })
                .then();

    }

    /**
     * Method that checks if an index is replicated in the majority of the servers
     * in the cluster.
     *
     * @param N Long that represents the index to check.
     *
     * @return boolean that tells if the majority has or has not that index replicated.
     * */
    private Mono<Boolean> majorityOfMatchIndexGreaterOrEqualThan (long N) {

        return Flux.fromIterable(this.matchIndex.values())
                .filter(index -> index >= N)
                .count()
                .map(count -> count + 1) // 1 because of the leader that is not in matchIndex Map
                .flatMap(count -> Mono.just(count >= this.raftProperties.getQuorum()));

    }

}
