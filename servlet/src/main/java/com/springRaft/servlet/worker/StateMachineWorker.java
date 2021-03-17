package com.springRaft.servlet.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* --------------------------------------------------- */

    @Override
    public void run() {

        log.info("\n\nState Machine Worker working...\n\n");

    }

    /* --------------------------------------------------- */

}
