package com.springraft.raft.config.startup;

import com.springraft.raft.consensusModule.ConsensusModule;
import com.springraft.raft.consensusModule.Follower;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Order(3)
@AllArgsConstructor
public class ConsensusModuleInitialization implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {
        Mono.just(this.applicationContext.getBean(Follower.class))
                .flatMap(this.consensusModule::setAndStartNewState)
                .subscribe();
    }

}
