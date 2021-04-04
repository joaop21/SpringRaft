package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("raft")
@AllArgsConstructor
public class RaftController implements InboundCommunication {

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

        return new ResponseEntity<>(this.appendEntries(appendEntries), HttpStatus.OK);

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

        return new ResponseEntity<>(this.requestVote(requestVote), HttpStatus.OK);

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

    /* --------------------------------------------------- */

    public ResponseEntity<?> clientRequestHandling(String command) {

        RequestReply reply = this.clientRequest(command);

        if (reply.getRedirect()) {

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("raft-leader", reply.getRedirectTo());

            return new ResponseEntity<>(httpHeaders, HttpStatus.TEMPORARY_REDIRECT);

        } else {

            return (ResponseEntity<?>) reply.getResponse();

        }

    }

}
