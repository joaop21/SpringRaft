package com.springRaft.servlet.worker;

import com.springRaft.servlet.consensusModule.ConsensusModule;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
public class ElectionTimeoutTimer implements Runnable {

    private final ConsensusModule consensusModule;

    @Override
    public void run() {
        System.out.println("TIMEOUT!!");
        this.consensusModule.work();
    }

}
