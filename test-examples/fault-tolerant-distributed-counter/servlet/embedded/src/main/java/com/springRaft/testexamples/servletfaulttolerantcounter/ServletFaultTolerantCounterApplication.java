package com.springRaft.testexamples.servletfaulttolerantcounter;

import com.springRaft.servlet.config.RaftProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication( scanBasePackages = {
		"com.springRaft.testexamples.servletfaulttolerantcounter", // Your application
		"com.springRaft.servlet.*",
		"com.springRaft.servlet.*.*"
} )
@EnableConfigurationProperties(RaftProperties.class)
@EnableJpaRepositories( basePackages={
		"com.springRaft.testexamples.servletfaulttolerantcounter",
		"com.springRaft.servlet.persistence.*"
} )
@EntityScan( basePackages={
		"com.springRaft.testexamples.servletfaulttolerantcounter",
		"com.springRaft.servlet.persistence.*"
} )
public class ServletFaultTolerantCounterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletFaultTolerantCounterApplication.class, args);
	}

}
