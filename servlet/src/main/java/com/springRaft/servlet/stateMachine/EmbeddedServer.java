package com.springRaft.servlet.stateMachine;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class EmbeddedServer implements StateMachineStrategy {

    @Override
    public void apply(String command) {

    }

}
