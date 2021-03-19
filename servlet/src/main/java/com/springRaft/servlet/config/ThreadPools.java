package com.springRaft.servlet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
public class ThreadPools {

    /**
     * Thread Pool for general purpose workers.
     *
     * @return TaskExecutor general purpose threads.
     * */
    @Bean(name = "generalPurposeExecutor")
    public TaskExecutor generalPurposeExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("GeneralWorker-");
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for peer workers.
     *
     * @return TaskExecutor dedicated to PeerWorker runnables.
     * */
    @Bean(name = "peerWorkersExecutor")
    public TaskExecutor peerWorkersTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("PeerWorker-");
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for async requests.
     *
     * @return TaskExecutor dedicated to requests.
     * */
    @Bean(name = "requestsExecutor")
    public TaskExecutor threadPoolTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("RequestTask-");
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for state machine thread.
     *
     * @return TaskExecutor dedicated to StateMachineWorker runnable.
     * */
    @Bean(name = "stateMachineTaskExecutor")
    public TaskExecutor stateMachineTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("FSMTask-");
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for scheduled tasks.
     *
     * @return ThreadPoolTaskScheduler dedicated to timers.
     * */
    @Bean(name = "transitionTaskExecutor")
    public ThreadPoolTaskScheduler transitionTaskExecutor(){

        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("TransitionTask-");

        return threadPoolTaskScheduler;
    }

}
