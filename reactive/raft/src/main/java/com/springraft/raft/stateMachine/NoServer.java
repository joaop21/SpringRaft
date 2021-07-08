package com.springraft.raft.stateMachine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Scope("singleton")
@ConditionalOnProperty(name = "raft.state-machine-strategy", havingValue = "NONE")
public class NoServer implements StateMachineStrategy {

    private final ResponseEntity<Void> templateResponse = new ResponseEntity<>(HttpStatus.OK);

    /* --------------------------------------------------- */

    @Override
    public Mono<Object> apply(String command) {
        return Mono.just(this.templateResponse);
    }

}
