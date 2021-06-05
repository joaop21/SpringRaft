package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.*;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Mono;

public interface RaftState {

    /**
     * Method for handling AppendEntries RPC
     *
     * @param appendEntries AppendEntries object sent from leader.
     *
     * @return Mono<AppendEntriesReply> Reply for the AppendEntries.
     * */
    Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries);

    /**
     * Method for handling AppendEntries replies.
     *
     * @param appendEntriesReply AppendEntriesReply object sent from other servers.
     * @param from String that identifies the server that sent the reply.
     *
     * @return A Mono to be subscribed.
     * */
    Mono<Void> appendEntriesReply(AppendEntriesReply appendEntriesReply, String from);

    /**
     * Method for handling RequestVote RPC.
     *
     * @param requestVote RequestVote Object sent from a candidate.
     *
     * @return RequestVoteReply Reply for the vote request.
     * */
    Mono<RequestVoteReply> requestVote(RequestVote requestVote);

    /**
     * Method for handling RequestVote replies.
     *
     * @param requestVoteReply RequestVoteReply object sent from other servers.
     *
     * @return A Mono to be subscribed.
     */
    Mono<Void> requestVoteReply(RequestVoteReply requestVoteReply);

    /**
     * Method for handling the replication of a client request.
     * @param command String command to replicate and apply to the FSM.
     *
     * @return RequestReply Reply for the income request.
     * */
    Mono<RequestReply> clientRequest(String command);

    /**
     * Method for getting the next message for a specific server.
     *
     * @param to String that represents the server.
     *
     * @return Pair<Message, Boolean> Message to send to the target server and a boolean that signals
     *      if this message is a heartbeat.
     * */
    Mono<Pair<Message, Boolean>> getNextMessage(String to);

    /**
     * Method for doing the work that it's required on startup.
     *
     * @return A Mono to be subscribed.
     * */
    Mono<Void> start();

}
