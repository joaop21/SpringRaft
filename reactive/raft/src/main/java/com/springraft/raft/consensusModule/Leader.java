package com.springraft.raft.consensusModule;

import com.springraft.raft.communication.message.*;
import com.springraft.raft.communication.outbound.OutboundManager;
import com.springraft.raft.config.RaftProperties;
import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import com.springraft.raft.stateMachine.StateMachineWorker;
import com.springraft.raft.stateMachine.WaitingRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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

    /* for each server, number of communications in transit left
        (initialized to raft.communications-in-transit) */
    private Map<String,Integer> missingCommunicationsInTransit;

    /* --------------------------------------------------- */

    public Leader(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            StateService stateService,
            LogService logService,
            RaftProperties raftProperties,
            TransitionManager transitionManager,
            OutboundManager outboundManager,
            StateMachineWorker stateMachineWorker,
            WaitingRequests waitingRequests
    ) {
        super(
                applicationContext, consensusModule,
                stateService, logService, raftProperties,
                transitionManager, outboundManager, stateMachineWorker,
                waitingRequests
        );
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
        this.missingCommunicationsInTransit = new HashMap<>();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return super.appendEntries(appendEntries);
    }

    @Override
    public Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from) {

        // Increment the communications in transit that left to reach the max
        if (this.missingCommunicationsInTransit.get(from) < this.raftProperties.getCommunicationsInTransit())
            this.missingCommunicationsInTransit.put(from, this.missingCommunicationsInTransit.get(from) + 1);

        if (appendEntriesReply == null)
            return this.sendNextAppendEntries(from, this.nextIndex.get(from), this.matchIndex.get(from));


        if (appendEntriesReply.getSuccess()) {

            return this.logService.getLastEntryIndex()
                    .map(index -> index + 1)
                    .flatMap(lastEntryIndex -> {

                        long nextIndex = this.nextIndex.get(from);
                        long matchIndex = this.matchIndex.get(from);

                        // compare last entry with next index for "from" server
                        // optimized 'if' (extended 'if' in servlet version)
                        if ((nextIndex != lastEntryIndex) && (matchIndex == (nextIndex - 1)))
                            return this.sendNextAppendEntries(from, nextIndex, matchIndex);


                        this.matchIndex.put(from, nextIndex - 1);
                        return this.setCommitIndex(from)
                                .then(this.sendNextAppendEntries(from, nextIndex, nextIndex - 1));

                    });

        } else {

            return this.stateService.getCurrentTerm()
                    .flatMap(currentTerm -> {

                        // if term is greater than mine, I should update it and transit to new follower
                        if (appendEntriesReply.getTerm() > currentTerm) {

                            return this.stateService.setState(appendEntriesReply.getTerm(), null)
                                    // clean leader's state
                                    .doOnNext(state -> this.cleanVolatileState())
                                    // transit to follower state
                                    .then(this.transitionManager.setNewFollowerState());

                        } else {

                            return Mono.defer(() -> {

                                long newNextIndex = this.nextIndex.get(from) - 1;

                                if (this.nextIndex.get(from) > this.nextIndex.get(from) - (appendEntriesReply.getToIndex() - appendEntriesReply.getFromIndex()))
                                    newNextIndex = this.nextIndex.get(from) - (appendEntriesReply.getToIndex() - appendEntriesReply.getFromIndex()) - 1;

                                this.nextIndex.put(from, newNextIndex);
                                this.matchIndex.put(from, (long) 0);

                                return this.sendNextAppendEntries(from, newNextIndex, 0);

                            });

                        }

                    });

        }

    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> {

                    RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);

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
                                .doOnNext(requestVoteReply -> this.cleanVolatileState())
                                .flatMap(requestVoteReply ->
                                        this.outboundManager.newFollowerState()
                                                .then(this.transitionManager.setNewFollowerState())
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
                            this.stateService.setState(requestVoteReply.getTerm(), null)
                                    // clean leader's state
                                    .doOnTerminate(this::cleanVolatileState)
                                    .then(this.outboundManager.newFollowerState())
                                    .then(this.transitionManager.setNewFollowerState())
                    );

    }

    @Override
    public Mono<RequestReply> clientRequest(String command) {

        return this.stateService.getCurrentTerm()
                .flatMap(currentTerm -> this.logService.insertEntry((Entry) this.applicationContext.getBean("ParametrisedEntry", currentTerm, command, true)))
                .flatMap(entry -> this.outboundManager.newClientRequest().then(Mono.just(entry)))
                .flatMap(entry -> this.waitingRequests.insertWaitingRequest(entry.getIndex()))
                .flatMap(Sinks.Empty::asMono)
                .map(response -> this.applicationContext.getBean(RequestReply.class, true, response, false, ""))
                .switchIfEmpty(Mono.just(this.applicationContext.getBean(RequestReply.class, false, new Object(), false, "")));
    }

    @Override
    public Mono<Void> start() {

        return this.reinitializeVolatileState()
                .then(
                        // issue empty AppendEntries in parallel to each of the other servers in the cluster
                        this.heartbeatAppendEntries().flatMap(this.outboundManager::sendAuthorityHeartbeat)
                )
                .doFirst(() -> log.info("LEADER"));

    }

    /* --------------------------------------------------- */

    @Override
    protected Mono<Void> postAppendEntries(AppendEntries appendEntries) {

        // deactivate PeerWorker
        return this.outboundManager.newFollowerState()
                // transit to follower state
                .then(this.transitionManager.setNewFollowerState())
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
                                    this.missingCommunicationsInTransit = new HashMap<>();
                                })
                                .doOnNext(serverName -> {
                                    this.nextIndex.put(serverName, (long) defaultNextIndex);
                                    this.matchIndex.put(serverName, (long) 0);
                                    this.missingCommunicationsInTransit.put(serverName, this.raftProperties.getCommunicationsInTransit());
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
        this.missingCommunicationsInTransit = new HashMap<>();

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private Mono<Void> sendNextAppendEntries(String to, long nextIndex, long matchIndex) {

        return this.logService.getEntryByIndex(nextIndex)
                .switchIfEmpty(Mono.just((Entry)this.applicationContext.getBean("NullEntry")))
                .flatMap(entry -> {

                    if (entry.getIndex() == null && matchIndex == (nextIndex - 1)) {
                        // if there is no entry in log then send heartbeat

                        return this.heartbeatAppendEntries()
                                .flatMap(heartbeat -> this.outboundManager.sendHeartbeat(heartbeat, to))
                                .doOnTerminate(() ->
                                        this.missingCommunicationsInTransit.put(to, this.missingCommunicationsInTransit.get(to) - 1)
                                );

                    } else if(entry.getIndex() == null) {
                        // if there is no entry and the logs are not matching

                        return this.heartbeatAppendEntries()
                                .flatMap(appendEntries -> this.outboundManager.sendAppendEntries(appendEntries,to))
                                .doOnTerminate(() ->
                                        this.missingCommunicationsInTransit.put(to, this.missingCommunicationsInTransit.get(to) - 1)
                                );

                    } else if(matchIndex == (nextIndex - 1)) {
                        // if there is an entry, and the logs are matching,
                        // send that entry

                        return this.logService.getEntriesBetweenIndexes(
                                nextIndex,
                                nextIndex + ((long) this.raftProperties.getEntriesPerCommunication() * this.missingCommunicationsInTransit.get(to))
                        )
                                .collectSortedList(Comparator.comparing(Entry::getIndex))
                                .flatMapMany(entries ->
                                    Flux.<List<? extends Entry>>create(sink -> {
                                        int messagesToSend = this.messagesToSend(entries.size());
                                        for (int i = 1 ; i <= messagesToSend ; i++) {
                                            List<? extends Entry> entriesSet =
                                                    entries.subList(
                                                            this.raftProperties.getEntriesPerCommunication() * (i - 1),
                                                            Math.min(entries.size(), this.raftProperties.getEntriesPerCommunication() + this.raftProperties.getEntriesPerCommunication() * (i - 1))
                                                    );
                                            sink.next(entriesSet);
                                        }
                                        sink.complete();
                                    })
                                )
                                .doOnNext(entries -> this.nextIndex.put(to, nextIndex + entries.size()))
                                .flatMap(entries -> this.createAppendEntries(entries.get(0), (List<Entry>) entries))
                                .flatMap(appendEntries -> this.outboundManager.sendAppendEntries(appendEntries,to))
                                .doOnTerminate(() ->
                                        this.missingCommunicationsInTransit.put(to, this.missingCommunicationsInTransit.get(to) - 1)
                                )
                                .then();

                    } else {
                        // if there is an entry, but the logs are not matching,
                        // send the appendEntries with no entries

                        return this.createAppendEntries(entry)
                                .flatMap(appendEntries -> this.outboundManager.sendAppendEntries(appendEntries,to))
                                .doOnTerminate(() ->
                                        this.missingCommunicationsInTransit.put(to, this.missingCommunicationsInTransit.get(to) - 1)
                                );

                    }

                });

    }

    /**
     * TODO
     * */
    private int messagesToSend(int entriesSize) {
        return entriesSize <= 0 ? 0 : ((entriesSize - 1) / this.raftProperties.getEntriesPerCommunication()) + 1;
    }

    /**
     * Method that creates an AppendEntries with no entries that represents an heartbeat.
     *
     * @return AppendEntries Message to pass to an up-to-date follower.
     * */
    private Mono<AppendEntries> heartbeatAppendEntries() {

        Mono<State> stateMono = this.stateService.getState();
        Mono<LogState> logStateMono = this.logService.getState();
        Mono<? extends Entry> lastEntryMono = this.logService.getLastEntry();

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
                .switchIfEmpty(Mono.just((Entry)this.applicationContext.getBean("EntryZero")));

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

    /* --------------------------------------------------- */

    /**
     * Method that checks whether it is possible to commit an entry,
     * and if so, commits it in the Log State as well as notifies the StateMachineWorker.
     *
     * @param from String that identifies the server that set a new matchIndex, to fetch its value.
     * */
    private Mono<Void> setCommitIndex(String from) {

        AtomicLong N = new AtomicLong(0);

        return Mono.just(this.matchIndex.get(from))
                .doOnNext(N::set)
                .flatMap(n ->
                        Mono.zip(
                                this.logService.getState(),
                                this.logService.getEntryByIndex(N.get()),
                                this.stateService.getCurrentTerm(),
                                this.majorityOfMatchIndexGreaterOrEqualThan(N.get())
                        )
                )
                .filter(tuple ->
                        N.get() > tuple.getT1().getCommittedIndex() &&
                        tuple.getT2().getTerm() == (long) tuple.getT3() &&
                        tuple.getT4()
                )
                    .flatMap(tuple -> {

                        LogState logState = tuple.getT1();
                        Entry entry = tuple.getT2();

                        logState.setCommittedIndex(N.get());
                        logState.setCommittedTerm(entry.getTerm());
                        logState.setNew(false);

                        return this.logService.saveState(logState);

                    })
                    // notify state machine of a new commit
                    .flatMap(logState -> this.stateMachineWorker.newCommit())
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
