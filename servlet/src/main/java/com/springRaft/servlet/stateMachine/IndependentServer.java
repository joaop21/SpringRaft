package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.worker.StateMachineWorker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        String[] tokens = command.split(";;;");
        String HTTPMethod = tokens[0];
        String endpoint = tokens[1];

        List<String> body = new ArrayList<>(Arrays.asList(tokens).subList(2, tokens.length));
        String json = String.join(";;;", body);

        log.info("\n\nInvoking: " + HTTPMethod + " on " + endpoint + " with " + json + "\n\n");

        // invoke in outbound

        return "Applied: " + HTTPMethod + " on " + endpoint + " with " + json +", to State Machine";

    }

    /* --------------------------------------------------- */

}
