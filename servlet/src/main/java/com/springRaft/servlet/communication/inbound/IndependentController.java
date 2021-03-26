package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.RequestReply;
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

@RestController
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "INDEPENDENT")
@AllArgsConstructor
public class IndependentController {

    private final RESTController restController;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(value = "/**/{[^\\.]*}")
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody(required = false) String body, HttpServletRequest request) throws URISyntaxException {

        String command = request.getMethod() + ";;;" + request.getRequestURI() + ";;;" + body;

        RequestReply reply = this.restController.clientRequest(command);

        if (reply.getRedirect()) {

            URI leaderURL = new URI("http:/" + reply.getRedirectTo() + request.getRequestURI());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(leaderURL);
            return new ResponseEntity<>(httpHeaders, HttpStatus.TEMPORARY_REDIRECT);

        } else {

            return (ResponseEntity<?>) reply.getResponse();

        }

    }

}
