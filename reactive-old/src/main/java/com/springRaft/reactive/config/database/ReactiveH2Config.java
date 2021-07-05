package com.springRaft.reactive.config.database;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@Profile("R2DBC-h2")
public class ReactiveH2Config {

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
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-R2DBC-h2.sql")));

        return initializer;
    }

}