package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.communication.outbound.OutboundContext;
import com.springRaft.reactive.communication.outbound.OutboundManager;
import com.springRaft.reactive.config.RaftProperties;
import com.springRaft.reactive.consensusModule.ConsensusModule;
import com.springRaft.reactive.worker.PeerWorker;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
@Scope("singleton")
@AllArgsConstructor
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* Map containing schedulers for a specific server */
    private static final Map<String,Scheduler> peerWorkers = new HashMap<>();

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
                .doOnNext(server -> {

                    // get worker object
                    PeerWorker worker = this.applicationContext.getBean(
                            PeerWorker.class,
                            this.applicationContext.getBean(OutboundContext.class),
                            this.applicationContext.getBean(ConsensusModule.class),
                            this.raftProperties,
                            server
                    );

                    // subscribe worker in outbound observer
                    this.outboundManager.subscribe(worker);

                    // Schedule worker on a dedicated scheduler
                    getPeerWorkerScheduler(server)
                            .schedule(worker);

                })
                .blockLast();

    }

    /* --------------------------------------------------- */

    /**
     * Method that gets a PeerWorker specific scheduler so it can run in the same environment.
     *
     * @param worker String that represents the server name.
     *
     * @return Scheduler for running the peerWorker.
     * */
    public static Scheduler getPeerWorkerScheduler(String worker) {

        Scheduler scheduler = peerWorkers.get(worker);
        if (scheduler == null) {
            scheduler = Schedulers.newSingle("PeerWorker");
            peerWorkers.put(worker, scheduler);
        }

        return scheduler;

    }

}
