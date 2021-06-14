package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.*;
import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class REST implements OutboundStrategy {

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Map which contain Webclient objects to use instead of creating one each time */
    private final Map<String, WebClient> webClientsMap = new HashMap<>();

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(String to, AppendEntries message) {
        return sendPostToServer(to, "appendEntries", message, AppendEntriesReply.class)
                .cast(AppendEntriesReply.class);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(String to, RequestVote message) {
        return sendPostToServer(to, "requestVote", message, RequestVoteReply.class)
                .cast(RequestVoteReply.class);
    }

    @Override
    public Mono<?> request(String command, String location) {

        return Mono.just(command.split(";;;"))
                .flatMap(tokens -> Mono.zip(
                        Mono.just(HttpMethod.valueOf(tokens[0])),
                        Mono.just(tokens[1]),
                        Mono.just(String.join(";;;", Arrays.asList(tokens).subList(2, tokens.length))))
                )
                .flatMap(tuple -> this.sendRequestToServer(location, tuple.getT1(), tuple.getT2(), tuple.getT3()));

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

        return this.getWebClient(to)
                .map(webClient ->
                        webClient
                                .post()
                                .uri("/raft/{route}", route)
                                .bodyValue(message)
                                .retrieve()
                )
                .flatMap(responseSpec -> responseSpec.bodyToMono(type))
                .timeout(this.raftProperties.getHeartbeat());
    }

    /**
     * Method that invokes an HTTP request in a specific server.
     *
     * @param to String that represents the server name.
     * @param method HTTP method to invoke in the server.
     * @param route String that represents the endpoint to invoke the request.
     * @param body String that contains the body of the request.
     *
     * @return Object which is the response to the request.
     * */
    private Mono<ResponseEntity<Object>> sendRequestToServer(String to, HttpMethod method, String route, String body) {

        return body.equals("null")
                ? this.getWebClient(to)
                        .map(webClient ->
                                webClient
                                        .method(method)
                                        .uri(route)
                                        .retrieve()
                        )
                        .flatMap(responseSpec -> responseSpec.toEntity(Object.class))

                : this.getWebClient(to)
                        .map(webClient ->
                                webClient
                                        .method(method)
                                        .uri(route)
                                        .bodyValue(body)
                                        .retrieve()
                        )
                        .flatMap(responseSpec -> responseSpec.toEntity(Object.class));

    }

    /**
     * Method that gets the WebClient object fot a specific target server.
     *
     * @param server String that represents the server name.
     *
     * @return WebClient object for a specific server.
     * */
    private Mono<WebClient> getWebClient(String server) {

        return Mono.create(monoSink -> {

            WebClient webClient = this.webClientsMap.get(server);
            if (webClient == null) {
                webClient = WebClient.create("http://" + server);
                this.webClientsMap.put(server, webClient);
            }

            monoSink.success(webClient);

        });

    }

}
