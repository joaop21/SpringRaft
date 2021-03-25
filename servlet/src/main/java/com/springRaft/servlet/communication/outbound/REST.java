package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.config.RaftProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
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

    @Override
    public Object request(String command) throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {

        String[] tokens = command.split(";;;");
        HttpMethod HTTPMethod = HttpMethod.valueOf(tokens[0]);
        URI endpoint = new URI("http://" + this.raftProperties.getApplicationServer() + tokens[1]);
        String body = String.join(";;;", Arrays.asList(tokens).subList(2, tokens.length));

        RequestEntity<?> request =
                body.equals("null")
                ? new RequestEntity<>(HTTPMethod, endpoint)
                : new RequestEntity<>(body, HTTPMethod, endpoint);

        return applyStateMachineCommand(request);
    }

    /* --------------------------------------------------- */

    /**
     * Method that invokes an HTTP request in a specific server, in a specific route, with a JSON message.
     *
     * @param to String that represents the server.
     * @param route String that represents the endpoint to invoke the HTTP request.
     * @param message String the represents the message to send to the server.
     * @param type Class of the object in the response.
     *
     * @return Message which is the return object as the response.
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

    /**
     * Method that invokes an HTTP request in the Application Server, applying the command in the state machine.
     *
     * @param requestEntity Request sent to server.
     *
     * @return Object which is the response to the request.
     * */
    private Object applyStateMachineCommand(RequestEntity<?> requestEntity)
            throws ExecutionException, InterruptedException
    {
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return restTemplate.exchange(requestEntity, Object.class);
                    } catch (HttpClientErrorException e) {
                        return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
                    }

                }, this.taskExecutor)
                .get();
    }

}
