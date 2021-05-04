package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
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
public class IndependentController {

    /* Main controller that communicates with consensus module */
    private final RaftController raftController;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* --------------------------------------------------- */

    /**
     * TODO
     *
     * @return*/
    @RequestMapping(value = "**")
    public Mono<ResponseEntity<?>> clientRequestEndpoint(@RequestBody(required = false) String body, ServerHttpRequest request) {

        return Mono.defer(() ->
                Mono.just(request.getMethod() + ";;;" + request.getPath() + ";;;" + body)
        )
                .flatMap(command ->

                    this.raftController.clientRequest(command)
                        .flatMap(requestReply -> {

                            if (requestReply.getSuccess()) {
                                // if the reply is successful just return the response

                                return Mono.just((ResponseEntity<?>) requestReply.getResponse());

                            } else {
                                // if the reply is not successful, check whether it is to redirect or not

                                if (requestReply.getRedirect()) {
                                    // send a request to leader, because I'm a follower

                                    return (Mono<ResponseEntity<?>>) this.outbound.request(command, requestReply.getRedirectTo());
                                                //.cast(ResponseEntity.class);

                                    //return Mono.just((ResponseEntity<?>) requestReply.getResponse());


                                } else {
                                    // if I'm a candidate, I cant redirect to anyone

                                    return Mono.defer(() -> {

                                        HttpHeaders httpHeaders = new HttpHeaders();
                                        httpHeaders.set(
                                                HttpHeaders.RETRY_AFTER,
                                                Long.toString(this.raftProperties.getHeartbeat().toMillis() / 1000)
                                        );

                                        return Mono.just(new ResponseEntity<>(httpHeaders, HttpStatus.SERVICE_UNAVAILABLE));

                                    });

                                }

                            }

                        })

                );

    }

}
