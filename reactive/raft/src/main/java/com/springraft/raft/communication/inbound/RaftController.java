package com.springraft.raft.communication.inbound;

import com.springraft.raft.communication.message.*;
import com.springraft.raft.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
     * @return Mono<ResponseEntity<AppendEntries>> A response entity which includes the reply
     * to the appendEntries communication.
     * */
    @RequestMapping(
            value = "/appendEntries",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public Mono<ResponseEntity<AppendEntriesReply>> appendEntriesEndpoint(@RequestBody AppendEntries appendEntries) {
        return this.appendEntries(appendEntries)
                //.doOnNext(reply -> log.info("\nAPPEND ENTRIES: " + appendEntries + "\n" + "RESPONSE: " + reply))
                .map(ResponseEntity::ok);
    }

    /**
     * Method that handles all the POST calls on "/raft/requestVote" endpoint.
     *
     * @param requestVote Object that represents a requestVote message.
     *
     * @return Mono<ResponseEntity<RequestVoteReply>> A response entity which includes the reply
     * to the requestVote communication.
     * */
    @RequestMapping(
            value = "/requestVote",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public Mono<ResponseEntity<RequestVoteReply>> requestVoteEndpoint(@RequestBody RequestVote requestVote) {
        return this.requestVote(requestVote)
                //.doOnNext(reply -> log.info("\nREQUEST VOTE: " + requestVote + "\n" + "RESPONSE: " + reply))
                .map(ResponseEntity::ok);
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(AppendEntries appendEntries) {
        return this.consensusModule.appendEntries(appendEntries);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return this.consensusModule.requestVote(requestVote);
    }

}
