package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.worker.StateMachineWorker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@AllArgsConstructor
public class IndependentServer implements StateMachineStrategy {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* --------------------------------------------------- */

    @Override
    public Object apply(String command) {

        log.info("\n\nApplying: " + command + " to State Machine\n\n");

        return "Applied: " + command + ", to State Machine";

    }

}
