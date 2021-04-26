package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.util.Pair;
import reactor.core.publisher.Mono;

public interface RaftState {

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
     */
    void requestVoteReply(RequestVoteReply requestVoteReply);

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
     * */
    void start();

}
