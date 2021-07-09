package com.springRaft.servlet.config.startup;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.communication.outbound.OutboundStrategy;
import com.springRaft.servlet.communication.outbound.REST;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.stateMachine.EmbeddedServer;
import com.springRaft.servlet.stateMachine.IndependentServer;
import com.springRaft.servlet.stateMachine.StateMachineStrategy;
import com.springRaft.servlet.stateMachine.StateMachineWorker;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@AllArgsConstructor
public class Strategies implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {

        // CLUSTER COMMUNICATION
        OutboundContext context = this.applicationContext.getBean(OutboundContext.class);
        OutboundStrategy outboundStrategy;

        switch (this.raftProperties.getClusterCommunicationStrategy().toUpperCase()) {

            case "REST":

            default:
                outboundStrategy = applicationContext.getBean(REST.class);
                context.setClusterCommunicationStrategy(outboundStrategy);
                break;

        }

        // APPLICATION COMMUNICATION
        switch (this.raftProperties.getApplicationCommunicationStrategy().toUpperCase()) {

            case "REST":

            default:
                outboundStrategy = applicationContext.getBean(REST.class);
                context.setApplicationCommunicationStrategy(outboundStrategy);
                break;

        }

        // STATE MACHINE
        StateMachineWorker worker = this.applicationContext.getBean(StateMachineWorker.class);

        StateMachineStrategy stateMachineStrategy = switch (this.raftProperties.getStateMachineStrategy().toUpperCase()) {
            case "EMBEDDED" -> this.applicationContext.getBean(EmbeddedServer.class);
            default -> this.applicationContext.getBean(IndependentServer.class);
        };

        worker.setStrategy(stateMachineStrategy);

    }

}
