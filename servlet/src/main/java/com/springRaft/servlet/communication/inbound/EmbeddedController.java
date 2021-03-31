package com.springRaft.servlet.communication.inbound;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@RestController
@RequestMapping("raft")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
@AllArgsConstructor
public class EmbeddedController {

    /* Main controller that communicates with consensus module */
    private final RaftController raftController;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(value = "/**/{[^\\.]*}")
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody(required = false) String body, HttpServletRequest request) throws URISyntaxException {

        String command = request.getMethod() + ";;;" + request.getRequestURI() + ";;;" + body;

        ResponseEntity<?> response = this.raftController.clientRequestHandling(request, command);
        if ( response.getStatusCode() == HttpStatus.TEMPORARY_REDIRECT) {

            String uri = Objects.requireNonNull(response.getHeaders().getLocation())
                    .toString()
                    .replaceFirst("/raft", "");

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(new URI(uri));

            response = new ResponseEntity<>(httpHeaders, response.getStatusCode());

        }

        return response;

    }

}
