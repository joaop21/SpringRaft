package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Lock")
@AllArgsConstructor
public class ServiceLock implements NodeService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(ServiceLock.class);

    private final NodeRepository repository;

    private final Lock lock = new ReentrantLock();

    /* --------------------------------------------------- */

    /*
    @Override
    public Mono<Node> get(String key) {

        return Mono.defer(() -> {

            this.lock.lock();
            log.info("IN");
            return this.repository.findNodeByKey(key)
                    .doOnSuccess(node -> {
                        this.lock.unlock();
                        log.info("OUT");
                    });
        })
                .subscribeOn(Schedulers.immediate());

    }*/

    @Override
    public Mono<Node> get(String key) {
        return Mono.empty();
    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {
        return Mono.empty();
    }

    @Override
    public Mono<Node> delete(String key) {
        return Mono.empty();
    }

}
