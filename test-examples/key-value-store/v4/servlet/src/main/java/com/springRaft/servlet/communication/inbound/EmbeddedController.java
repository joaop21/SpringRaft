package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
@RequestMapping("raft")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
@AllArgsConstructor
public class EmbeddedController {

    /* Main controller that communicates with consensus module */
    private final RaftController raftController;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(value = "/**/{[^\\.]*}")
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody(required = false) String body, HttpServletRequest request) {

        String command = request.getMethod() + ";;;" + request.getRequestURI() + ";;;" + body;

        try {

            ResponseEntity<?> response = this.raftController.clientRequestHandling(command);

            if ( response.getStatusCode() == HttpStatus.TEMPORARY_REDIRECT) {

                String location = Objects.requireNonNull(response.getHeaders().get("raft-leader")).get(0);

                command = command.replaceFirst("/raft", "");

                response = (ResponseEntity<?>) this.outbound.request(command, location);

            }

            return response;

        } catch (Exception e) {

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(HttpHeaders.RETRY_AFTER, Long.toString(this.raftProperties.getHeartbeat().toMillis() / 1000));
            return new ResponseEntity<>(httpHeaders, HttpStatus.SERVICE_UNAVAILABLE);

        }

    }

}
