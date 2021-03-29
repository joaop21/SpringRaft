package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class EmbeddedServer extends SMStrategyContext implements StateMachineStrategy {

    /* --------------------------------------------------- */

    public EmbeddedServer(RaftProperties raftProperties, OutboundContext outbound) {
        super(raftProperties, outbound);
    }

    /* --------------------------------------------------- */

    @Override
    public Object apply(String command) {

        return super.apply(command);

    }

}
