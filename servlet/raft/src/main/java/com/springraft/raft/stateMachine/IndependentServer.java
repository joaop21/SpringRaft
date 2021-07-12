package com.springraft.raft.stateMachine;

import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.config.RaftProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "INDEPENDENT")
public class IndependentServer extends SMStrategyContext implements StateMachineStrategy {

    /* --------------------------------------------------- */

    public IndependentServer(RaftProperties raftProperties, OutboundContext outbound) {
        super(raftProperties, outbound);
    }

    /* --------------------------------------------------- */

    @Override
    public Object apply(String command) {

        return super.apply(command);

    }

}
