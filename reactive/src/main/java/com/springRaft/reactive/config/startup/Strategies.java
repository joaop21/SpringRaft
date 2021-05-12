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

        // COMMUNICATION
        Mono<OutboundStrategy> communicationStrategy =
                Mono.just(this.raftProperties.getCommunicationStrategy().toUpperCase())
                    .map(strategy ->
                        switch (strategy) {
                            default -> applicationContext.getBean(REST.class);
                        }
                    );

        Mono.zip(communicationStrategy, Mono.just(this.applicationContext.getBean(OutboundContext.class)))
                .doOnNext(tuple -> tuple.getT2().setCommunicationStrategy(tuple.getT1()))
                .block();

    }
}
