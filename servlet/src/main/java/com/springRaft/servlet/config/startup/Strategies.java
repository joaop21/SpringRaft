package com.springRaft.servlet.config.startup;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.communication.outbound.OutboundStrategy;
import com.springRaft.servlet.communication.outbound.REST;
import com.springRaft.servlet.config.RaftProperties;
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
    public void run(ApplicationArguments args) throws Exception {

        // COMMUNICATION
        OutboundContext context = this.applicationContext.getBean(OutboundContext.class);
        OutboundStrategy strategy;

        switch (this.raftProperties.getCommunicationStrategy().toUpperCase()) {

            case "REST":

            default:
                strategy = applicationContext.getBean(REST.class);
                context.setCommunicationStrategy(strategy);
                break;

        }

    }

}
