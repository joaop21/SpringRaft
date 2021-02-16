package com.springRaft.servlet.worker;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineWorker.class);

    /* --------------------------------------------------- */

    @SneakyThrows
    @Override
    public void run() {

        LOGGER.info("Entrei");

        Thread.sleep(5000);

        LOGGER.info("Terminei a execução");

    }

}
