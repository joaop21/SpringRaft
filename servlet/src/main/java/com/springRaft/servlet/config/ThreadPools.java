package com.springRaft.servlet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPools {

    @Bean(name = "threadPoolTaskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for state machine thread
     *
     * @return TaskExecutor dedicated to state machine threads
     * */
    @Bean(name = "stateMachineTaskExecutor")
    public TaskExecutor stateMachineTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();

        return executor;
    }

    /**
     * Thread Pool for scheduled tasks
     *
     * @return ThreadPoolTaskScheduler dedicated to timers
     * */
    @Bean(name = "timerTaskScheduler")
    public ThreadPoolTaskScheduler timerTaskScheduler(){

        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);

        return threadPoolTaskScheduler;
    }
}
