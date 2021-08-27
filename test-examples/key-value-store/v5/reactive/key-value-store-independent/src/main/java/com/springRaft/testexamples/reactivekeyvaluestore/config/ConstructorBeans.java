package com.springRaft.testexamples.reactivekeyvaluestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
public class ConstructorBeans {

    @Bean
    @Scope("prototype")
    public Lock newLock() {
        return new ReentrantLock();
    }

    @Bean(name = "requestScheduler")
    public Scheduler requestsScheduler() {
        return Schedulers.newBoundedElastic(2000,1000, "RequestScheduler");
    }

    @Bean(name = "repoScheduler")
    public Scheduler jdbcScheduler() {
        return Schedulers.newBoundedElastic(10, Integer.MAX_VALUE, "jdbcScheduler");
    }

}
