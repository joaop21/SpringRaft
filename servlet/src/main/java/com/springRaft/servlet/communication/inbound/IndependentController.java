package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.RequestReply;
import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnExpression("'${raft.state-machine-strategy}'.equals('INDEPENDENT') || '${raft.state-machine-strategy}'.equals('NONE')")
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
     * @param request HttpServletRequest object that contains all the information about the request.
     *
     * @return ResponseEntity<?> A response entity which includes the reply to the request made.
     * */
    @RequestMapping(value = "/**/{[^\\.]*}")
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody(required = false) String body, HttpServletRequest request) {

        String command = request.getMethod() + ";;;" + request.getRequestURI() + ";;;" + body;

        try {

            RequestReply reply = this.clientRequest(command);

            if (reply.getRedirect())
                return (ResponseEntity<?>) this.outbound.request(command, reply.getRedirectTo());

            return reply.getSuccess()
                    ? (ResponseEntity<?>) reply.getResponse()
                    : new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {

            e.printStackTrace();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(HttpHeaders.RETRY_AFTER, Long.toString(this.raftProperties.getHeartbeat().toMillis() / 1000));
            return new ResponseEntity<>(httpHeaders, HttpStatus.SERVICE_UNAVAILABLE);

        }

    }

    /* --------------------------------------------------- */

    @Override
    public RequestReply clientRequest(String command) {
        return this.consensusModule.clientRequest(command);
    }
}
