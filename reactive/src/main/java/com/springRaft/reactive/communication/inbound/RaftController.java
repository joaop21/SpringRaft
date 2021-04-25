package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("raft")
@AllArgsConstructor
public class RaftController implements InboundCommunication {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RaftController.class);

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @RequestMapping(
            value = "/requestVote",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json"
    )
    public Mono<ResponseEntity<RequestVoteReply>> requestVoteEndpoint(@RequestBody RequestVote requestVote) {

        //log.info("\nREQUEST: " + requestVote.toString() + "\n" + "RESPONSE: " + reply);

        log.info("\nREQUEST: " + requestVote.toString());

        return this.requestVote(requestVote)
                .map(ResponseEntity::ok);

    }

    /* --------------------------------------------------- */

    @Override
    public Mono<RequestVoteReply> requestVote(RequestVote requestVote) {
        return Mono.defer(() -> Mono.just(new RequestVoteReply((long) 5, false)));
    }

}
