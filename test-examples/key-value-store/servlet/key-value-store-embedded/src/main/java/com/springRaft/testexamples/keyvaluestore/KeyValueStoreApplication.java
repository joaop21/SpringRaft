package com.springRaft.testexamples.keyvaluestore;

import com.springRaft.servlet.config.RaftProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication( scanBasePackages = {
		"com.springRaft.testexamples.keyvaluestore.*",
		"com.springRaft.servlet.*",
		"com.springRaft.servlet.*.*"
} )
@EnableConfigurationProperties(RaftProperties.class)
@EnableJpaRepositories( basePackages={
		"com.springRaft.testexamples.keyvaluestore.node",
		"com.springRaft.servlet.persistence.*"
} )
@EntityScan( basePackages={
		"com.springRaft.testexamples.keyvaluestore.node",
		"com.springRaft.servlet.persistence.*"
} )
public class KeyValueStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeyValueStoreApplication.class, args);
	}

}
