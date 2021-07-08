package com.springraft.raft.stateMachine;

import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.config.RaftProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
public class EmbeddedServer extends SMStrategyContext {

    /* --------------------------------------------------- */

    public EmbeddedServer(RaftProperties raftProperties, OutboundContext outbound, @Value("${server.port}") Integer port) {
        super(raftProperties, outbound, "localhost:" + port);
        System.out.println(super.toString());
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Object> apply(String command) {
        return super.apply(command);
    }
}
