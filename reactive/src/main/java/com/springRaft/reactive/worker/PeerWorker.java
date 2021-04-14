package com.springRaft.reactive.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Scope("prototype")

public class PeerWorker implements Runnable {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(PeerWorker.class);

    /* --------------------------------------------------- */

    @Override
    public void run() {

        while (true) {

            Flux.range(0,10)
                    .map(i -> i+5)
                    .filter(i -> i%2 == 0)
                    .subscribe(i -> log.info(String.valueOf(i)));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

        }

    }

}
