package com.springRaft.testexamples.keyvaluestore.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "node.service")
@ConstructorBinding
@Getter
@ToString
public class ServiceProperties {

    private final String strategy;

    /* --------------------------------------------------- */

    public ServiceProperties(@DefaultValue("Sync") String strategy) {
        this.strategy = strategy;
    }

}
