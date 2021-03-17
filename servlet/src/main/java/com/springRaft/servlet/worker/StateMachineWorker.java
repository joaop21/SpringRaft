package com.springRaft.servlet.worker;

import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Service to access persisted log repository */
    private final LogService logService;

    private LogState logState;

    /* --------------------------------------------------- */

    @Autowired
    public StateMachineWorker(LogService logService) {
        this.logService = logService;
        this.logState = this.logService.getState();
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

        log.info("\n\nState Machine Worker working...\n\n");

        // start working



    }

    /* --------------------------------------------------- */

}
