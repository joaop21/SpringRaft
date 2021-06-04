package com.springRaft.testexamples.reactivekeyvaluestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReactiveKeyValueStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveKeyValueStoreApplication.class, args);
	}

}
