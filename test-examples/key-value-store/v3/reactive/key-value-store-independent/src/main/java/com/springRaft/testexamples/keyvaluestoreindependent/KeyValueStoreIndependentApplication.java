package com.springRaft.testexamples.keyvaluestoreindependent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KeyValueStoreIndependentApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeyValueStoreIndependentApplication.class, args);
	}

}
