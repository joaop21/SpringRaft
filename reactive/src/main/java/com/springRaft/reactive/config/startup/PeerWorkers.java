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

import java.time.Duration;

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

        OutboundContext context = this.applicationContext.getBean(OutboundContext.class);
        ConsensusModule module = this.applicationContext.getBean(ConsensusModule.class);

        Flux.fromIterable(this.raftProperties.getCluster())
                .doOnNext(server -> {
                    PeerWorker worker = this.applicationContext.getBean(
                            PeerWorker.class,
                            context,
                            module,
                            this.raftProperties,
                            server
                    );
                    this.outboundManager.subscribe(worker);
                    this.scheduler.schedule(worker);
                })
                .subscribe();

    }

}
