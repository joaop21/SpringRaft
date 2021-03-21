package com.springRaft.servlet.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RaftProperties.class);

    /* Address of this server */
    private final InetSocketAddress host;

    /* List of Addresses of cluster */
    private final List<InetSocketAddress> cluster;

    /* Majority of members from a peer set */
    private final Integer quorum;

    /* Minimum Timeout to trigger an election */
    private final Duration electionTimeoutMin;

    /* Maximum Timeout to trigger an election */
    private final Duration electionTimeoutMax;

    /* Timeout for direct communications */
    private final Duration heartbeat;

    /* Strategy for communication */
    private final String communicationStrategy;

    /* --------------------------------------------------- */

    public RaftProperties(
            @DefaultValue("localhost:8080") String hostname,
            @DefaultValue({"localhost:8001", "localhost:8002", "localhost:8003"})
                    List<String> cluster,
            @DefaultValue("0") @DurationUnit(ChronoUnit.MILLIS)
                    Duration electionTimeoutMin,
            @DefaultValue("0") @DurationUnit(ChronoUnit.MILLIS)
                    Duration electionTimeoutMax,
            @DefaultValue("0") @DurationUnit(ChronoUnit.MILLIS)
                    Duration heartbeat,
            @DefaultValue("REST") String communicationStrategy
    ) {

        this.electionTimeoutMin = electionTimeoutMin;
        this.electionTimeoutMax = electionTimeoutMax;
        this.heartbeat = heartbeat;

        this.host = getAddressFromHostname(hostname);

        this.cluster = new ArrayList<>();
        for (String hoststring : cluster)
            this.cluster.add(getAddressFromHostname(hoststring));

        int clusterSize = this.cluster.size() + 1;
        this.quorum = (clusterSize / 2) + 1;

        this.communicationStrategy = communicationStrategy;

        log.info(this.toString());

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private InetSocketAddress getAddressFromHostname(String hostname) {
        String[] split = hostname.split(":");
        return InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1]));
    }

    /**
     * TODO
     * */
    public String AddressToString(InetSocketAddress address) {
        return address.getHostName() + ":" + address.getPort();
    }

    /* --------------------------------------------------- */

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("\n*****************************************\n")
                .append("\nThis server is operating from:\n\t")
                .append(host.getHostName()).append(":")
                .append(host.getPort()).append("\n\n");

        builder.append("The cluster includes:");
        for (InetSocketAddress address : cluster)
            builder.append("\n\t")
                    .append(address.getHostName())
                    .append(":")
                    .append(address.getPort());

        builder.append("\n\n")
                .append("The Quorum size is ").append(this.quorum)
                .append(" servers\n")
                .append("\nElection Properties:\n")
                .append("\t Timeout is between [")
                .append(electionTimeoutMin.toMillis()).append(",")
                .append(electionTimeoutMax.toMillis()).append("]ms\n")
                .append("\n")
                .append("Heartbeat has ")
                .append(heartbeat.toMillis()).append("ms of duration\n")
                .append("\nCommunication strategy is: ").append(communicationStrategy).append("\n")
                .append("\n*****************************************");

        return builder.toString();
    }
}
