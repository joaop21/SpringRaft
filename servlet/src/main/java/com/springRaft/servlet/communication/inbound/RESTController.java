package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.consensusModule.Candidate;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("raft")
@AllArgsConstructor
public class RESTController implements InboundCommunication {

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(value = "/appendEntries", method = RequestMethod.POST)
    public ResponseEntity<Boolean> appendEntriesEndpoint(HttpServletRequest request) {

        // check ip address of client invoking this endpoint
        // log.info("Client IP: " + request.getRemoteAddr() + ":" + request.getRemotePort());

        return new ResponseEntity<>(this.appendEntries(), HttpStatus.OK);
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
    public Boolean appendEntries() {
        return true;
    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        return this.consensusModule.requestVote(requestVote);

    }

}
