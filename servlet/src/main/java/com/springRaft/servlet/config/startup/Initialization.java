package com.springRaft.servlet.config.startup;

import com.springRaft.servlet.consensusModule.ConsensusModule;
import com.springRaft.servlet.consensusModule.Follower;
import com.springRaft.servlet.worker.StateTransition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class Initialization implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Task Executor for submit workers to execution */
    private final TaskExecutor taskExecutor;

    /* --------------------------------------------------- */

    @Autowired
    public Initialization(
            ApplicationContext applicationContext,
            ConsensusModule consensusModule,
            @Qualifier(value = "stateMachineTaskExecutor") TaskExecutor taskExecutor
    ) {

        this.applicationContext = applicationContext;
        this.consensusModule = consensusModule;
        this.taskExecutor = taskExecutor;

    }

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {
        StateTransition transition = applicationContext
                .getBean(StateTransition.class, applicationContext, consensusModule, Follower.class);
        taskExecutor.execute(transition);
    }

}
