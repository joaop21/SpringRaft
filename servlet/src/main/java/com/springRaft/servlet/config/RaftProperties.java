package com.springRaft.servlet.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("raft")
@ConstructorBinding
@Getter
public class RaftProperties {

    /* List of Addresses of cluster */
    private final List<InetSocketAddress> cluster;

    /* Timeout to trigger an election */
    private final Duration electionTimeout;

    /* --------------------------------------------------- */

    public RaftProperties(
            @DefaultValue({"localhost:8001", "localhost:8002", "localhost:8003"})
                    List<String> cluster,
            @DefaultValue("0") @DurationUnit(ChronoUnit.MILLIS)
                    Duration electionTimeout
            ) {

        this.electionTimeout = electionTimeout;

        this.cluster = new ArrayList<>();
        for (String hoststring : cluster) {
            String[] split = hoststring.split(":");
            this.cluster.add(InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1])));
        }

    }

}
