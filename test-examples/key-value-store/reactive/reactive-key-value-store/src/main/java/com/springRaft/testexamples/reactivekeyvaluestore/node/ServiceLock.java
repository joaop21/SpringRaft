package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope("singleton")
@AllArgsConstructor
public class ServiceLock {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(ServiceLock.class);

    private final NodeRepository repository;

    private final Lock lock = new ReentrantLock();

    /* --------------------------------------------------- */

    /*
    public Mono<Node> get(String key) {

        return Mono.defer(() -> {

            this.lock.lock();
            log.info("Entrei");
            return this.repository.findNodeByKey(key)
                    .doOnSuccess(node -> {
                        this.lock.unlock();
                        log.info("Saí");
                    });
        })
                .subscribeOn(Schedulers.immediate());

    }*/

}
