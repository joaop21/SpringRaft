package com.springraft.raft.communication.inbound;

import com.springraft.raft.communication.message.*;
import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.consensusModule.ConsensusModule;
import com.springraft.raft.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "INDEPENDENT")
@AllArgsConstructor
public class IndependentController implements ClientInboundCommunication {

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* --------------------------------------------------- */

    /**
     * Method that handles all the calls on whatever endpoint not defined in any other controller.
     *
     * @param body String that represents the body of the request.
     * @param request ServerHttpRequest object that contains all the information about the request.
     *
     * @return Mono<ResponseEntity<?>> A response entity which includes the reply to the request made.
     * */
    @RequestMapping(value = "**")
    public Mono<ResponseEntity<?>> clientRequestEndpoint(
            @RequestBody(required = false) String body,
            ServerHttpRequest request)
    {

        String command = request.getMethod() + ";;;" + request.getPath() + ";;;" + body;
        return this.clientRequest(command)
                .flatMap(requestReply -> {

                    if (requestReply.getSuccess()) {
                        // if the reply is successful just return the response

                        return Mono.just((ResponseEntity<?>) requestReply.getResponse());

                    } else {
                        // if the reply is not successful, check whether it is to redirect or not

                        if (requestReply.getRedirect()) {
                            // send a request to leader, because I'm a follower

                            return (Mono<ResponseEntity<?>>) this.outbound.request(command, requestReply.getRedirectTo());


                        } else {
                            // if I'm a candidate, I cant redirect to anyone

                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.set(
                                    HttpHeaders.RETRY_AFTER,
                                    Long.toString(this.raftProperties.getHeartbeat().toMillis() / 1000)
                            );

                            return Mono.just(new ResponseEntity<>(httpHeaders, HttpStatus.SERVICE_UNAVAILABLE));

                        }

                    }

                });

    }

    /* --------------------------------------------------- */

    @Override
    public Mono<RequestReply> clientRequest(String command) {
        return this.consensusModule.clientRequest(command);
    }

}
