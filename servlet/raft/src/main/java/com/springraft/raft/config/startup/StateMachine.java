package com.springraft.raft.config.startup;

import com.springraft.raft.stateMachine.StateMachineWorker;
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
public class StateMachine implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Task Executor for submit worker for execution */
    private final TaskExecutor taskExecutor;

    /* --------------------------------------------------- */

    @Autowired
    public StateMachine(
            ApplicationContext applicationContext,
            @Qualifier(value = "stateMachineTaskExecutor") TaskExecutor taskExecutor
    ) {
        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
    }

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {

        StateMachineWorker worker = this.applicationContext.getBean(StateMachineWorker.class);

        this.taskExecutor.execute(worker);

    }
}

