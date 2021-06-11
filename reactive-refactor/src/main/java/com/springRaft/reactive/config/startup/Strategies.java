package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.communication.outbound.OutboundStrategy;
import com.springRaft.reactive.communication.outbound.REST;
import com.springRaft.reactive.config.RaftProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Order(1)
@AllArgsConstructor
public class Strategies implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {

        // CLUSTER COMMUNICATION
        Mono<OutboundStrategy> clusterCommunicationStrategy =
                Mono.just(this.raftProperties.getClusterCommunicationStrategy().toUpperCase())
                        .map(strategy ->
                                switch (strategy) {
                                    default -> applicationContext.getBean(REST.class);
                                }
                        );

        Mono.zip(clusterCommunicationStrategy, Mono.just(this.applicationContext.getBean(OutboundContext.class)))
                .doOnNext(tuple -> tuple.getT2().setClusterCommunicationStrategy(tuple.getT1()))
                .block();


        // APPLICATION COMMUNICATION
        Mono<OutboundStrategy> applicationCommunicationStrategy =
                Mono.just(this.raftProperties.getApplicationCommunicationStrategy().toUpperCase())
                        .map(strategy ->
                                switch (strategy) {
                                    default -> applicationContext.getBean(REST.class);
                                }
                        );

        Mono.zip(applicationCommunicationStrategy, Mono.just(this.applicationContext.getBean(OutboundContext.class)))
                .doOnNext(tuple -> tuple.getT2().setApplicationCommunicationStrategy(tuple.getT1()))
                .block();

    }
}
