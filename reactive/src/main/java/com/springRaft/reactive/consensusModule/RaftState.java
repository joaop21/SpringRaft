package com.springRaft.reactive.consensusModule;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.util.Pair;
import reactor.core.publisher.Mono;

public interface RaftState {

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
