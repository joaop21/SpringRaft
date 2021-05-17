package com.springRaft.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class WorkerSchedulers {

    /**
     * Scheduler for general purpose workers.
     *
     * @return Scheduler for general purpose threads.
     * */
    @Bean(name = "generalPurposeScheduler")
    public Scheduler generalPurposeScheduler() {

        return Schedulers.newBoundedElastic(5, 5, "GeneralScheduler");

    }

    /**
     * Scheduler for async requests.
     *
     * @return TaskExecutor dedicated to requests.
     * */
    @Bean(name = "requestsScheduler")
    public Scheduler threadPoolScheduler() {

        return Schedulers.newBoundedElastic(100, Integer.MAX_VALUE, "RequestTask");

    }

    /**
     * Scheduler for state machine thread.
     *
     * @return Scheduler dedicated to StateMachineWorker runnable.
     * */
    @Bean(name = "stateMachineScheduler")
    public Scheduler stateMachineScheduler() {

        return Schedulers.newBoundedElastic(1, 1, "FSMTask");

    }

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
