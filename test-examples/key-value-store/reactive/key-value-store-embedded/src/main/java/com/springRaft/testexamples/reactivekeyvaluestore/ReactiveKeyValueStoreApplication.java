package com.springRaft.testexamples.reactivekeyvaluestore;

import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.persistence.log.EntryRepository;
import com.springRaft.reactive.persistence.log.LogStateRepository;
import com.springRaft.reactive.persistence.state.StateRepository;
import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication( scanBasePackages = {
		"com.springRaft.testexamples.reactivekeyvaluestore.*",
		"com.springRaft.testexamples.reactivekeyvaluestore.*.*",
		"com.springRaft.reactive.*",
		"com.springRaft.reactive.*.*"
} )
@EnableConfigurationProperties(RaftProperties.class)
@EnableR2dbcRepositories(basePackageClasses = {
		// SpringRaft
		StateRepository.class, LogStateRepository.class, EntryRepository.class,
		// key-value-store
		NodeRepository.class
})
@ConfigurationPropertiesScan
public class ReactiveKeyValueStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveKeyValueStoreApplication.class, args);
	}

}
