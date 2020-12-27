package com.springraft.testexamples.reactivestack.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class CommunicationService {

    /*--------------------------------------------------------------------------------*/

    /* TODO */
    private final Log log = LogFactory.getLog(getClass());

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
    public void increment() {
        if (leader)
            sendToGroups("increment");
    }

    /**
     * TODO
     * */
    public void decrement() {
        if (leader)
            sendToGroups("decrement");
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    private void sendToGroups(String route) {

        group.remove((Integer)serverPort);

        Flux.fromIterable(group)
                .flatMap(server -> callOtherService(server, route))
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    /**
     * TODO
     * */
    private Mono<Long> callOtherService(int port, String route) {

        WebClient webClient = WebClient.create("http://localhost:" + port);

        return webClient.post()
                .uri("/counter/{route}", route)
                .retrieve()
                .bodyToMono(Long.class);

    }

}
