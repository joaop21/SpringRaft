package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.consensusModule.ConsensusModule;
import com.springRaft.reactive.consensusModule.Follower;
import com.springRaft.reactive.worker.StateTransition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
@Order(3)
public class ConsensusModuleInitialization implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Task Executor for submit workers to execution */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    @Autowired
    public ConsensusModuleInitialization(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            @Qualifier(value = "generalPurposeScheduler") Scheduler scheduler
    ) {
        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.scheduler = scheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {
        Mono.just(this.applicationContext.getBean(StateTransition.class, this.applicationContext, this.consensusModule, Follower.class))
                .doOnNext(this.scheduler::schedule)
                .block();
    }

}
