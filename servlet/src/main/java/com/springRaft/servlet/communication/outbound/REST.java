package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.config.RaftProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
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
    public AppendEntriesReply appendEntries(String to, AppendEntries message)
            throws InterruptedException, ExecutionException, TimeoutException
    {

        return (AppendEntriesReply) sendToServer(to, "/appendEntries", message, AppendEntriesReply.class);

    }

    @Override
    public RequestVoteReply requestVote(String to, RequestVote message)
            throws InterruptedException, ExecutionException, TimeoutException
    {

        return (RequestVoteReply) sendToServer(to, "/requestVote", message, RequestVoteReply.class);

    }



    /**
     * TODO
     * */
    private Message sendToServer(String to, String route, Message message, Class<? extends Message> type)
            throws InterruptedException, ExecutionException, TimeoutException
    {

        return CompletableFuture
                .supplyAsync(() -> {

                    String endpoint = "http://" + to + "/raft" + route;

                    return restTemplate.postForEntity(endpoint, message, type)
                            .getBody();

                }, this.taskExecutor)
                .get(this.raftProperties.getHeartbeat().toMillis(), TimeUnit.MILLISECONDS);

    }

}
