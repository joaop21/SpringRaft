package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
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
