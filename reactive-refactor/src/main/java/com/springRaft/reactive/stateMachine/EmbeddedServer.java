package com.springRaft.reactive.stateMachine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "EMBEDDED")
public class EmbeddedServer implements StateMachineStrategy {
    @Override
    public Mono<Object> apply(String command) {
        return null;
    }
}
