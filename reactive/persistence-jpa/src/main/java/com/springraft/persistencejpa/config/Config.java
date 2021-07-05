package com.springraft.persistencejpa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class Config {

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int connectionPoolSize;

    /* --------------------------------------------------- */

    @Bean(name = "jdbcScheduler")
    public Scheduler jdbcScheduler() {
        return Schedulers.newBoundedElastic(connectionPoolSize, Integer.MAX_VALUE, "jdbcScheduler");
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
