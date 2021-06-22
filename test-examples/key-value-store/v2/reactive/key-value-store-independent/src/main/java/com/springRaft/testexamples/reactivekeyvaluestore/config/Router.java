package com.springRaft.testexamples.reactivekeyvaluestore.config;

import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Router {

    @Bean
    public RouterFunction<ServerResponse> functionalRoutes(NodeController nodeController) {
        return RouterFunctions.route()
                .path("/v2/keys", builder -> builder
                        .GET("/{key}", nodeController::get)
                        .PUT("/{key}", nodeController::upsert)
                        .DELETE("/{key}", nodeController::delete)
                )
                .build();
    }

}
