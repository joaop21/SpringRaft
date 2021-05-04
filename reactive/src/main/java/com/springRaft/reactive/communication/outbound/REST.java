package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.RaftProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
public class REST implements OutboundStrategy {

    /* Scheduler for submit workers to execution */
    private final Scheduler scheduler;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    public REST(
            @Qualifier(value = "requestsScheduler") Scheduler scheduler,
            RaftProperties raftProperties
    ) {
        this.scheduler = scheduler;
        this.raftProperties = raftProperties;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(String to, AppendEntries message) {
        return (Mono<AppendEntriesReply>) sendPostToServer(to, "appendEntries", message, AppendEntriesReply.class);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(String to, RequestVote message) {
        return (Mono<RequestVoteReply>) sendPostToServer(to, "requestVote", message, RequestVoteReply.class);
    }

    @Override
    public Mono<?> request(String command, String location) {

        return null;

    }

    /* --------------------------------------------------- */

    /**
     * Method that invokes an HTTP POST request in a specific server, in a specific route, with a JSON message.
     * Used mostly in Raft algorithm communications.
     *
     * @param to String that represents the server.
     * @param route String that represents the endpoint to invoke the HTTP request.
     * @param message String the represents the message to send to the server.
     * @param type Class of the object in the response.
     *
     * @return Message which is the return object as the response.
     * */
    private Mono<? extends Message> sendPostToServer(String to, String route, Message message, Class<? extends Message> type) {
        return WebClient.create("http://" + to)
                .post()
                .uri("/raft/{route}", route)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(type)
                .timeout(this.raftProperties.getHeartbeat())
                .subscribeOn(this.scheduler);
    }

}
