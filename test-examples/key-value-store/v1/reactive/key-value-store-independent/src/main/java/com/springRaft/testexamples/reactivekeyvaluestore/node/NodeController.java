package com.springRaft.testexamples.reactivekeyvaluestore.node;

import com.springRaft.testexamples.reactivekeyvaluestore.node.service.NodeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
public class NodeController {

    private final NodeService service;

    /* --------------------------------------------------- */

    public Mono<ServerResponse> get(ServerRequest serverRequest) {

        AtomicReference<Mono<ServerResponse>> serverResponse = new AtomicReference<>();

        return this.service.get(serverRequest.pathVariable("key"))
                .doOnSuccess(node -> {

                    Map<String,Object> response = new HashMap<>();

                    if (node == null) {

                        response.put("message", "Key not found");
                        response.put("key", serverRequest.pathVariable("key"));
                        serverResponse.set(
                                ServerResponse.status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(response))
                        );

                    } else {

                        response.put("action", "get");
                        response.put("node", node);
                        serverResponse.set(
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(response))
                        );

                    }
                })
                .then(Mono.defer(serverResponse::get));

    }

    /* --------------------------------------------------- */

    public Mono<ServerResponse> upsert(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(String.class)
                .flatMap(body -> this.service.upsert(serverRequest.pathVariable("key"), body))
                .flatMap(list -> {

                    Map<String,Object> response = new HashMap<>();

                    if (list.size() == 1) {

                        response.put("action", "set");
                        response.put("node", list.get(0));

                        return ServerResponse.created(serverRequest.uri())
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response));

                    } else {

                        response.put("action", "set");
                        response.put("node", list.get(1));
                        response.put("prevNode", list.get(0));

                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(response));

                    }

                });


    }

    /* --------------------------------------------------- */

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        AtomicReference<Mono<ServerResponse>> responseEntity = new AtomicReference<>();

        return this.service.delete(serverRequest.pathVariable("key"))
                .doOnSuccess(node -> {

                    Map<String,Object> response = new HashMap<>();

                    if (node == null) {

                        response.put("message", "Key not found");
                        response.put("key", serverRequest.pathVariable("key"));
                        responseEntity.set(
                                ServerResponse.status(HttpStatus.NOT_FOUND)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(response))
                        );

                    } else {

                        response.put("action", "delete");
                        response.put("node", new Node(node.getCreatedIndex(), node.getKey(), null, true));
                        response.put("prevNode", node);
                        responseEntity.set(
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(response))
                        );

                    }

                })
                .then(Mono.defer(responseEntity::get));

    }

}
