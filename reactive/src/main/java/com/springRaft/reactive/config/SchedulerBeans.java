package com.springRaft.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerBeans {

    /**
     * Scheduler for scheduled tasks.
     *
     * @return Scheduler dedicated to timers.
     * */
    @Bean(name = "transitionTaskScheduler")
    public Scheduler transitionTaskScheduler(){

        return Schedulers.newBoundedElastic(1, 1, "TransitionTask");

    }

}
