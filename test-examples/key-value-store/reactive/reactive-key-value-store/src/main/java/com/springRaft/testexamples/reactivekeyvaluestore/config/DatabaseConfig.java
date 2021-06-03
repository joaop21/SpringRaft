package com.springRaft.testexamples.reactivekeyvaluestore.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Bean for configuring @Transactional annotation on methods and classes.
     *
     * @param connectionFactory Factory of connections needed for the Transaction Manager.
     *
     * @return ReactiveTransactionManager that manages transactions in a reactive environment.
     * */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Bean for creating an operator for programmatic transaction control.
     *
     * @param transactionManager The transaction environment.
     *
     * @return TransactionalOperator Operator for applying to reactive pipelines in order to wrap
     * a series of operations into a transaction unit.
     * */
    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

}
