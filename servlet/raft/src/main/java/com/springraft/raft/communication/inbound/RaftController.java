package com.springraft.raft.communication.inbound;

import com.springraft.raft.communication.message.AppendEntries;
import com.springraft.raft.communication.message.AppendEntriesReply;
import com.springraft.raft.communication.message.RequestVote;
import com.springraft.raft.communication.message.RequestVoteReply;
import com.springraft.raft.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("raft")
@ConditionalOnProperty(name = "raft.cluster-communication-strategy", havingValue = "REST")
@AllArgsConstructor
public class RaftController implements RaftInboundCommunication {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RaftController.class);

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    /**
     * Method that handles all the POST calls on "/raft/appendEntries" endpoint.
     *
     * @param appendEntries Object that represents an appendEntries message.
     *
     * @return ResponseEntity<AppendEntries> A response entity which includes the reply
     * to the appendEntries communication.
     * */
    @RequestMapping(
            value = "/appendEntries",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AppendEntriesReply> appendEntriesEndpoint(@RequestBody AppendEntries appendEntries) {

        AppendEntriesReply reply = this.appendEntries(appendEntries);

        // log.info("\nREQUEST: " + appendEntries.toString() + "\n" + "RESPONSE: " + reply);

        return new ResponseEntity<>(reply, HttpStatus.OK);

    }

    /**
     * Method that handles all the POST calls on "/raft/requestVote" endpoint.
     *
     * @param requestVote Object that represents a requestVote message.
     *
     * @return ResponseEntity<RequestVoteReply> A response entity which includes the reply
     * to the requestVote communication.
     * */
    @RequestMapping(
            value = "/requestVote",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<RequestVoteReply> requestVoteEndpoint(@RequestBody RequestVote requestVote) {

        RequestVoteReply reply = this.requestVote(requestVote);

        // log.info("\nREQUEST: " + requestVote.toString() + "\n" + "RESPONSE: " + reply);

        return new ResponseEntity<>(reply, HttpStatus.OK);

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

}

