package com.springraft.raft.stateMachine;

public interface StateMachineStrategy {

    /**
     * Method for applying a command to the State Machine, depending on the strategy.
     *
     * @param command Command to apply to the State Machine.
     *
     * @return Object that represents the response.
     * */
    Object apply(String command);

}
