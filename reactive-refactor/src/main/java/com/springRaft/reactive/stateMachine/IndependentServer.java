package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.config.RaftProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "INDEPENDENT")
public class IndependentServer extends SMStrategyContext implements StateMachineStrategy {

    /* --------------------------------------------------- */

    public IndependentServer(RaftProperties raftProperties, OutboundContext outbound) {
        super(raftProperties, outbound, raftProperties.getApplicationServer());
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Object> apply(String command) {
        return super.apply(command);
    }
}
