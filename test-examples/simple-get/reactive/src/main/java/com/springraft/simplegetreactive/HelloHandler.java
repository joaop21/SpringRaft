package com.springraft.simplegetreactive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "endpoint.type", havingValue = "Functional")
public class HelloHandler {

    private static final String response = "Hello World! From Reactive";

    public Mono<ServerResponse> hello(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue(response));
    }

}
