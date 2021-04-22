package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.Message;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class REST implements OutboundStrategy {

    /* --------------------------------------------------- */

    @Override
    public Mono<RequestVoteReply> requestVote(String to, RequestVote message) {

        return (Mono<RequestVoteReply>) sendPostToServer(to, "requestVote", message, RequestVoteReply.class);

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
                .bodyToMono(type);
    }

}
