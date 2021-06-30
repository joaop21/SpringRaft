package com.springRaft.servlet.config.startup;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.communication.outbound.OutboundManager;
import com.springRaft.servlet.communication.outbound.PeerWorker;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class PeerWorkers implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Task Executor for submit workers to execution */
    private final TaskExecutor taskExecutor;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* Publisher of messages */
    private final OutboundManager outboundManager;

    /* --------------------------------------------------- */

    @Autowired
    public PeerWorkers(
            ApplicationContext applicationContext,
            @Qualifier(value = "peerWorkersExecutor") TaskExecutor taskExecutor,
            RaftProperties raftProperties,
            OutboundManager outboundManager
    ) {

        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
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
            PeerWorker worker = this.applicationContext.getBean(PeerWorker.class, context, module, raftProperties, addr);
            this.outboundManager.subscribe(addr, worker);
            this.taskExecutor.execute(worker);
        }

    }

}
