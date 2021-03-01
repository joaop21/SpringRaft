package com.springRaft.servlet.communication.outbound;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class OutboundContext implements OutboundStrategy {

    /* Outbound communication Strategy to use */
    private OutboundStrategy communicationStrategy;

    /* --------------------------------------------------- */

    public OutboundContext(ApplicationContext applicationContext) {
        // REST is the default strategy for outbound communication
        this.communicationStrategy = applicationContext.getBean(REST.class);
    }

    /* --------------------------------------------------- */

    public void setCommunicationStrategy(OutboundStrategy communication) {
        this.communicationStrategy = communication;
    }

    /* --------------------------------------------------- */

    @Override
    public Boolean appendEntries(String to) {
        return this.communicationStrategy.appendEntries(to);
    }

    @Override
    public Boolean requestVote() {
        return this.communicationStrategy.requestVote();
    }

}
