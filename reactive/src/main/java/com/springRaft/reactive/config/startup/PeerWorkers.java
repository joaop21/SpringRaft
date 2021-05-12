package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import com.springRaft.reactive.worker.PeerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

@Component
@Order(2)
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Scheduler for submit workers to execution */
    private final Scheduler scheduler;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* --------------------------------------------------- */

    @Autowired
    public PeerWorkers(
            ApplicationContext applicationContext,
            @Qualifier(value = "peerWorkersScheduler") Scheduler scheduler,
            RaftProperties raftProperties,
            OutboundManager outboundManager
    ) {

        this.applicationContext = applicationContext;
        this.scheduler = scheduler;
        this.raftProperties = raftProperties;
        this.outboundManager = outboundManager;
    }

    /* --------------------------------------------------- */

    /**
     * Startup component that creates the peer workers, subscribe them to new messages and
     * execute them in the respective Scheduler.
     *
     * @param args Arguments of the application.
     * */
    @Override
    public void run(ApplicationArguments args) {

        Flux.fromIterable(this.raftProperties.getCluster())
                .map(server ->
                        this.applicationContext.getBean(
                            PeerWorker.class,
                            this.applicationContext.getBean(OutboundContext.class),
                            this.applicationContext.getBean(ConsensusModule.class),
                            this.raftProperties,
                            server
                        )
                )
                .doOnNext(worker -> {
                    this.outboundManager.subscribe(worker);
                    this.scheduler.schedule(worker);
                })
                .blockLast();

    }

}
