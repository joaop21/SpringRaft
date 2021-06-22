package com.springraft.simplegetreactive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@ConditionalOnProperty(name = "endpoint.type", havingValue = "Functional")
public class HelloRouter {

    @Bean
    public RouterFunction<ServerResponse> functionalRoutes(HelloHandler helloHandler) {
        return RouterFunctions
                .route()
                .GET("/hello", helloHandler::hello)
                .build();
    }

}
