package com.springraft.raft.config.startup;

import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.communication.outbound.OutboundStrategy;
import com.springraft.raft.communication.outbound.REST;
import com.springraft.raft.config.RaftProperties;
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

    }

}

