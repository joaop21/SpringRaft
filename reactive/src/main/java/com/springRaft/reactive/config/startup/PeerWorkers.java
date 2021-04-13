package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.worker.PeerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;

@Component
@Order(2)
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Scheduler for submit workers to execution */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    @Autowired
    public PeerWorkers(
            ApplicationContext applicationContext,
            @Qualifier(value = "peerWorkersScheduler") Scheduler scheduler
    ) {

        this.applicationContext = applicationContext;
        this.scheduler = scheduler;

    }

    /* --------------------------------------------------- */

    /**
     * Startup component that creates the peer workers, subscribe them to new messages and
     * execute them in the respective Scheduler.
     *
     * @param args Arguments of the application.
     * */
    @Override
    public void run(ApplicationArguments args) {

        PeerWorker worker = this.applicationContext.getBean(PeerWorker.class);

        this.scheduler.schedule(worker);

    }

}
