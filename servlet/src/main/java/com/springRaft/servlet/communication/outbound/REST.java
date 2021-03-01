package com.springRaft.servlet.communication.outbound;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class REST implements OutboundStrategy {

    /* Object that helps creating REST requests */
    private final RestTemplate restTemplate;

    /* Task Executor for submit workers to execution */
    private final TaskExecutor taskExecutor;

    public REST(
            RestTemplate restTemplate,
            @Qualifier(value = "requestsExecutor") TaskExecutor taskExecutor
    ) {
        this.restTemplate = restTemplate;
        this.taskExecutor = taskExecutor;
    }

    /* --------------------------------------------------- */

    @Override
    public Boolean appendEntries(String to) {

        try {
            return CompletableFuture.supplyAsync(() -> this.post(to, "/appendEntries"), taskExecutor)
                    .exceptionally(e -> {
                        System.out.println(e.getCause().toString());
                        return false;
                    })
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }

    }

    @Override
    public Boolean requestVote() {
        try {
            return this.post("localhost:8003", "/requestVote");
        } catch (ResourceAccessException e) {
            return false;
        }
    }

    /**
     * TODO
     * */
    private Boolean post(String host, String route) throws ResourceAccessException {

        String endpoint = "http://" + host + "/raft" + route;
        return restTemplate.postForObject(endpoint, null, Boolean.class);

    }

}
