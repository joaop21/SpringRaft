package com.springraft.testexamples.reactivestack;

import com.springraft.testexamples.reactivestack.models.Counter;
import com.springraft.testexamples.reactivestack.services.CounterService;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.ReactiveTransactionManager;

@SpringBootApplication
public class ReactiveStackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveStackApplication.class, args);
	}

	/**
	 * Connection Factory for accessing database
	 * */
	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));

		return initializer;
	}

	/**
	 * TODO
	 * */
	@Bean
	public CommandLineRunner demo(CounterService service) {

		return (args) -> {
			// save a Counter for further utilization
			// ID - 1, VALUE - 0, VERSION - 0
			service.save(new Counter(0,0,0))
					.subscribe();
		};
	}

}
