package com.springRaft.reactive.stateMachine;

import reactor.core.publisher.Mono;

public interface StateMachineStrategy {

    /**
     * Method for applying a command to the State Machine, depending on the strategy.
     *
     * @param command Command to apply to the State Machine.
     *
     * @return Object that represents the response.
     * */
    Mono<Object> apply(String command);

}
