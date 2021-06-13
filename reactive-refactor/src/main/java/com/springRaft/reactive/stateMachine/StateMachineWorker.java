package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.persistence.log.LogService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@AllArgsConstructor
public class StateMachineWorker implements CommitmentSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Strategy for applying commands to the State Machine */
    private StateMachineStrategy strategy;

    /* Service to access persisted log repository */
    private final LogService logService;

    /* --------------------------------------------------- */

    public void setStrategy(StateMachineStrategy stateMachineStrategy) {
        this.strategy = stateMachineStrategy;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Void> newCommit() {
        return Mono.empty();
    }

    /* --------------------------------------------------- */



}
