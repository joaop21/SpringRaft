package com.springRaft.servlet.worker;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable {

    @SneakyThrows
    @Override
    public void run() {

        System.out.println("Entrei");
        Thread.sleep(5000);
        System.out.println("Terminei a execução");

    }

}
