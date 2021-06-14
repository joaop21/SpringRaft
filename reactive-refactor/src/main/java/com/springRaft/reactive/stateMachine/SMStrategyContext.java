package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@ToString
public class SMStrategyContext {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* Name of the application server to invoke requests */
    private final String targetServer;

    /* --------------------------------------------------- */

    public Mono<Object> apply(String command) {
        return Mono.empty();
    }

}
