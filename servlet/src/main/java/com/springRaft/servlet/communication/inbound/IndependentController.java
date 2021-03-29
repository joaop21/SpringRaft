package com.springRaft.servlet.communication.inbound;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@RestController
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "INDEPENDENT")
@AllArgsConstructor
public class IndependentController {

    /* Main controller that communicates with consensus module */
    private final RaftController raftController;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(value = "/**/{[^\\.]*}")
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody(required = false) String body, HttpServletRequest request) throws URISyntaxException {

        String command = request.getMethod() + ";;;" + request.getRequestURI() + ";;;" + body;

        return this.raftController.clientRequestHandling(request, command);

    }

}
