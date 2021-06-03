package com.springRaft.testexamples.reactivekeyvaluestore.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class H2Config {

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
