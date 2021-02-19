package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.outbound.OutboundCommunication;
import com.springRaft.servlet.communication.outbound.REST;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineWorker.class);

    private final OutboundCommunication outboundCommunication;

    public StateMachineWorker(ApplicationContext applicationContext) {
        this.outboundCommunication = applicationContext.getBean(REST.class);
    }

    /* --------------------------------------------------- */

    @SneakyThrows
    @Override
    public void run() {

        LOGGER.info("Started");

        Thread.sleep(5000);

        this.outboundCommunication.appendEntries();
        this.outboundCommunication.requestVote();

        LOGGER.info("Finished");

    }

}
