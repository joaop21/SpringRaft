package com.springraft.simplegetreactive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "endpoint.type", havingValue = "Annotated")
public class HelloController {

    private static final String response = "Hello World! From Servlet";

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just(response);
    }

}
