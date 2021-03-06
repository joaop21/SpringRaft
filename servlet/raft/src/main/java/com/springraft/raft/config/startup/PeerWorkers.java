package com.springraft.raft.config.startup;

import com.springraft.raft.communication.outbound.OutboundContext;
import com.springraft.raft.communication.outbound.OutboundManager;
import com.springraft.raft.communication.outbound.PeerWorker;
import com.springraft.raft.config.RaftProperties;
import com.springraft.raft.consensusModule.ConsensusModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Task Executor for submit workers to execution */
    private final TaskExecutor peerWorkerTaskExecutor;

    /* Task Executor for submit general tasks from peer workers */
    private final TaskExecutor generalTaskExecutor;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* --------------------------------------------------- */

    @Autowired
    public PeerWorkers(
            ApplicationContext applicationContext,
            @Qualifier(value = "peerWorkersExecutor") TaskExecutor peerWorkerTaskExecutor,
            @Qualifier("generalPurposeExecutor") ThreadPoolTaskExecutor generalTaskExecutor,
            RaftProperties raftProperties,
            OutboundManager outboundManager
    ) {

        this.applicationContext = applicationContext;
        this.peerWorkerTaskExecutor = peerWorkerTaskExecutor;
        this.generalTaskExecutor = generalTaskExecutor;
        this.raftProperties = raftProperties;
        this.outboundManager = outboundManager;
    }

    /* --------------------------------------------------- */

    /**
     * Startup component that creates the peer workers, subscribe them to new messages and
     * execute them in the thread pool.
     *
     * @param args Arguments of the application.
     * */
    @Override
    public void run(ApplicationArguments args) {

        OutboundContext context = this.applicationContext.getBean(OutboundContext.class);
        ConsensusModule module = this.applicationContext.getBean(ConsensusModule.class);

        for(String addr : this.raftProperties.getCluster()) {
            PeerWorker worker = this.applicationContext.getBean(PeerWorker.class, context, module, raftProperties, addr, this.generalTaskExecutor);
            this.outboundManager.subscribe(addr, worker);
            this.peerWorkerTaskExecutor.execute(worker);
        }

    }

}

