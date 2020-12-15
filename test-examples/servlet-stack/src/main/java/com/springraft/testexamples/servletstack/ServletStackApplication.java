package com.springraft.testexamples.servletstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ServletStackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletStackApplication.class, args);
	}

}
