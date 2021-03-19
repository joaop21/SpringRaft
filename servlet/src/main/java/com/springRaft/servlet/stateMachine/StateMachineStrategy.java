package com.springRaft.servlet.stateMachine;

public interface StateMachineStrategy {

    /**
     * Method for applying a command to the State Machine, depending on the strategy.
     *
     * @param command Command to apply to the State Machine.
     * */
    void apply(String command);

}
