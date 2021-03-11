package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * TODO
     * */
    @RequestMapping(
            value = "/request",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<String> clientRequestEndpoint(@RequestBody String command) {

        System.out.println("\n\nCommand: " + command + "\n\n");

        return new ResponseEntity<>(command, HttpStatus.OK);

        //return new ResponseEntity<>(this.requestVote(requestVote), HttpStatus.OK);

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
    public void clientRequest(String command) {

        this.consensusModule.clientRequest(command);

    }

}
