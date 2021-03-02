package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.config.RaftProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class REST implements OutboundStrategy {

    /* Object that helps creating REST requests */
    private final RestTemplate restTemplate;

    /* Task Executor for submit workers to execution */
    private final TaskExecutor taskExecutor;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    public REST(
            RestTemplate restTemplate,
            @Qualifier(value = "requestsExecutor") TaskExecutor taskExecutor,
            RaftProperties raftProperties
    ) {
        this.restTemplate = restTemplate;
        this.taskExecutor = taskExecutor;
        this.raftProperties = raftProperties;
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
    public RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException {
        return CompletableFuture
                .supplyAsync(() -> {
                    String endpoint = "http://" + to + "/raft/requestVote";
                    return restTemplate.postForEntity(endpoint, message, RequestVoteReply.class).getBody();
                }, this.taskExecutor)
                .get(this.raftProperties.getHeartbeat().toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * TODO
     * */
    private Boolean post(String host, String route) throws ResourceAccessException {

        String endpoint = "http://" + host + "/raft" + route;
        return restTemplate.postForObject(endpoint, null, Boolean.class);

    }

}
