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
import reactor.core.publisher.Flux;

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
    public void run(ApplicationArguments args) throws Exception {

        // COMMUNICATION
        Flux.just(this.raftProperties.getCommunicationStrategy().toUpperCase())
                .doOnNext(strategy -> {
                    OutboundContext context = this.applicationContext.getBean(OutboundContext.class);

                    switch (strategy) {
                        default -> context.setCommunicationStrategy(applicationContext.getBean(REST.class));
                    }
                })
                .subscribe();

    }
}
