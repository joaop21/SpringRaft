package com.springraft.testexamples.reactivestack.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CommunicationService {

    /*--------------------------------------------------------------------------------*/

    /* TODO */
    @Value("${server.port}")
    private int serverPort;

    /* TODO */
    @Value("${group.leader}")
    private boolean leader;

    /* TODO */
    @Value("${group.members}")
    private List<Integer> group;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public Flux<Long> increment() {
         return leader
                 ? sendToGroups("increment")
                 : Flux.just((long)0) ;
    }

    /**
     * TODO
     *
     * */
    public Flux<Long> decrement() {
        return leader
                ? sendToGroups("decrement")
                : Flux.just((long)0) ;
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    private Flux<Long> sendToGroups(String route) {

        group.remove((Integer)serverPort);

        return Flux.fromIterable(group)
                .flatMap(server -> callOtherService(server, route));
    }

    /**
     * TODO
     * */
    private Mono<Long> callOtherService(int port, String route) {

        return WebClient.create("http://localhost:" + port)
                .post()
                .uri("/counter/{route}", route)
                .retrieve()
                .bodyToMono(Long.class);

    }

}
