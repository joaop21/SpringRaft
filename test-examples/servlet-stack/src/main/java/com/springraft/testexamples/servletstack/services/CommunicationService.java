package com.springraft.testexamples.servletstack.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CommunicationService {

    /*--------------------------------------------------------------------------------*/

    /* TODO */
    @Autowired
    private RestTemplate restTemplate;

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

    /* TODO */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public void increment() {
        if (leader)
            sendToGroup("/increment");
    }

    /**
     * TODO
     * */
    public void decrement() {
        if (leader)
            sendToGroup("/decrement");
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    private void sendToGroup(String route) {

        List<CompletableFuture<Long>> allFutures = new ArrayList<>();

        for (int port : group) {
            if (port != serverPort)
                allFutures.add(callOtherService(port, route));
        }

        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

    }

    /**
     * TODO
     * */
    @Async
    protected CompletableFuture<Long> callOtherService(int port, String route) {

        String endpoint = "http://localhost:" + port + "/counter" + route;
        Long responseObj = restTemplate.postForObject(endpoint, null, Long.class);
        return CompletableFuture.completedFuture(responseObj);

    }

}
