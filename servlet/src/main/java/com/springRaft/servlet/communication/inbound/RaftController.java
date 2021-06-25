package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.consensusModule.ConsensusModule;
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
     * TODO
     * */
    @RequestMapping(
            value = "/appendEntries",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AppendEntriesReply> appendEntriesEndpoint(@RequestBody AppendEntries appendEntries) {

        AppendEntriesReply reply = this.appendEntries(appendEntries);

        //log.info("\nREQUEST: " + appendEntries.toString() + "\n" + "RESPONSE: " + reply);

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

        RequestVoteReply reply = this.requestVote(requestVote);

        //log.info("\nREQUEST: " + requestVote.toString() + "\n" + "RESPONSE: " + reply);

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
