package com.springraft.testexamples.servletstack;

import com.springraft.testexamples.servletstack.models.Counter;
import com.springraft.testexamples.servletstack.services.CounterService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;



@SpringBootApplication
@EnableAsync
public class ServletStackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletStackApplication.class, args);
	}

	/**
	 *
	 * */
	@Bean
	public CommandLineRunner demo(CounterService service) {

		return (args) -> {
			// save a Counter for further utilization
			// ID - 1, VALUE - 0
			service.save(new Counter(1, 0, 0));
		};

	}

}
