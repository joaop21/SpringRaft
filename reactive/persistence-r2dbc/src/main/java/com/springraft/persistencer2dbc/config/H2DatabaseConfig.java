package com.springraft.persistencer2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@EnableTransactionManagement
@Profile("R2DBC-h2")
public class H2DatabaseConfig {

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

    /**
     * Bean for a Connection Factory Initializer in order to open connection to the database and
     * make the first changes in the database.
     *
     * @param connectionFactory Factory of connections injected in this initializer.
     *
     * @return ConnectionFactoryInitializer for the framework to initialize the database access.
     * */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-h2.sql")));

        return initializer;
    }
}
