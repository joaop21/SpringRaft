package com.springRaft.reactive.worker;

import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.outbound.MessageSubscriber;
import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")

public class PeerWorker implements Runnable, MessageSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(PeerWorker.class);

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* String of the address of the target server */
    private final String targetServerName;

    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            String targetServerName
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServerName;


    }

    /* --------------------------------------------------- */

    @Override
    public void newMessage() {

    }

    @Override
    public void clearMessages() {

    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        this.outbound.requestVote(this.targetServerName, new RequestVote((long) 0, "myserver", (long) 0, (long) 0))
                .doOnNext(reply -> log.info(reply.toString()))
                .subscribe();

    }

}
