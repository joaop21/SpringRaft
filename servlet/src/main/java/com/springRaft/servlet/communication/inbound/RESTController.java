package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("raft")
@AllArgsConstructor
public class RESTController implements InboundCommunication {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RESTController.class);

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(
            value = "/appendEntries",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AppendEntriesReply> appendEntriesEndpoint(@RequestBody AppendEntries appendEntries) {

        log.info(appendEntries.toString());

        AppendEntriesReply reply = this.appendEntries(appendEntries);

        log.info(reply.toString());

        return new ResponseEntity<>(reply, HttpStatus.OK);
    }

    /**
     * TODO
     * */
    @RequestMapping(
            value = "/requestVote",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<RequestVoteReply> requestVoteEndpoint(@RequestBody RequestVote requestVote) {

        log.info(requestVote.toString());

        RequestVoteReply reply = this.requestVote(requestVote);

        log.info(reply.toString());

        return new ResponseEntity<>(reply, HttpStatus.OK);

    }

    /**
     * TODO
     * */
    @RequestMapping(
            value = "/request",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> clientRequestEndpoint(@RequestBody String command) throws URISyntaxException {

        RequestReply reply = this.clientRequest(command);

        if (reply.getRedirect()) {

            URI leaderURL = new URI("http:/" + reply.getRedirectTo() + "/raft/request");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(leaderURL);
            return new ResponseEntity<>(httpHeaders, HttpStatus.TEMPORARY_REDIRECT);

        } else {

            Map<String,?> replyEntity = Map.of("success",reply.getSuccess(), "response", reply.getResponse());

            return new ResponseEntity<>(replyEntity, HttpStatus.CREATED);

        }

    }

    /* --------------------------------------------------- */

    @Override
    public AppendEntriesReply appendEntries(AppendEntries appendEntries) {

        return this.consensusModule.appendEntries(appendEntries);

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        return this.consensusModule.requestVote(requestVote);

    }

    @Override
    public RequestReply clientRequest(String command) {

        return this.consensusModule.clientRequest(command);

    }

}
