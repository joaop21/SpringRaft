package com.springRaft.reactive.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "raft")
@ConstructorBinding
@Getter
public class RaftProperties {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(RaftProperties.class);

    /* Address of this server */
    private final String host;

    /* List of Addresses of cluster */
    private final List<String> cluster;

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

    /* Strategy for state machine */
    private final String stateMachineStrategy;

    /* Application Server address */
    private final String applicationServer;

    /* Maximum of entries that a communication can carry */
    private final Integer entriesPerCommunication;

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
            @DefaultValue("REST") String communicationStrategy,
            @DefaultValue("INDEPENDENT") String stateMachineStrategy,
            @DefaultValue("localhost:9002") String applicationServer,
            @DefaultValue("10") int entriesPerCommunication
    ) {

        this.electionTimeoutMin = electionTimeoutMin;
        this.electionTimeoutMax = electionTimeoutMax;
        this.heartbeat = heartbeat;

        this.host = hostname;

        this.cluster = new ArrayList<>();
        for (String addr : cluster)
            if (!addr.equals(hostname))
                this.cluster.add(addr);


        int clusterSize = this.cluster.size() + 1;
        this.quorum = (clusterSize / 2) + 1;

        this.communicationStrategy = communicationStrategy;
        this.stateMachineStrategy = stateMachineStrategy;

        this.applicationServer = applicationServer;

        this.entriesPerCommunication = entriesPerCommunication;

        log.info(this.toString());

    }

    /* --------------------------------------------------- */


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("\n*****************************************\n")
                .append("\nThis server is operating from:\n\t")
                .append(host).append("\n\n");

        builder.append("The cluster includes:");
        for (String address : cluster)
            builder.append("\n\t").append(address);

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
                .append("\nCommunication:\n")
                .append("\tStrategy: ").append(communicationStrategy).append("\n")
                .append("\tEntries per Communication: ").append(entriesPerCommunication).append("\n")
                .append("\nState Machine strategy is: ").append(stateMachineStrategy).append("\n")
                .append("\nApplication Server is: ").append(applicationServer).append("\n")
                .append("\n*****************************************");

        return builder.toString();
    }
}
