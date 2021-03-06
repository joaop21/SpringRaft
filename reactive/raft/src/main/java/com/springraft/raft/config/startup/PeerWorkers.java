package com.springraft.raft.config.startup;

import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.communication.outbound.OutboundManager;
import com.springraft.raft.communication.outbound.PeerWorker;
import com.springraft.raft.config.RaftProperties;
import com.springraft.raft.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
@AllArgsConstructor
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* Map containing the PeerWorkers responsible for the servers */
    private final Map<String, PeerWorker> peerWorkers = new HashMap<>();

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

                    // put worker in the map if doesn't exist
                    if (this.peerWorkers.putIfAbsent(server, worker) == null)
                        // subscribe worker in outbound observer
                        this.outboundManager.subscribe(server, worker);

                })
                .blockLast();

    }

}

