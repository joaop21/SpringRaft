package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Lock")
public class ServiceLock implements NodeService {

    private final NodeRepository repository;

    private final Lock lock ;

    /* Task Executor for submit workers to execution */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    public ServiceLock(
            NodeRepository repository,
            Lock lock,
            @Qualifier("requestScheduler") Scheduler scheduler
    ) {
        this.repository = repository;
        this.lock = lock;
        this.scheduler = scheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {

        return Mono.defer(() -> {
            this.lock.lock();
            return this.repository.findNodeByKey(key)
                    .doOnSuccess(node -> this.lock.unlock());
        })
                .subscribeOn(this.scheduler);

    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {

        return Mono.defer(() -> {

            AtomicReference<List<Node>> list = new AtomicReference<>(new ArrayList<>());

            this.lock.lock();
            return this.repository.findNodeByKey(key)
                    .flatMap(node ->
                            repository.deleteNodeByKey(key)
                                    .doOnSuccess(result -> list.get().add(node))
                    )
                    .then(this.repository.save(new Node(key,text.replaceFirst("value=", ""))))
                    .doOnNext(savedNode -> list.get().add(savedNode))
                    .flatMap(savedNode -> Mono.defer(() -> Mono.just(list.get())))
                    .doOnSuccess(node -> this.lock.unlock());

        })
                .subscribeOn(this.scheduler);

    }

    @Override
    public Mono<Node> delete(String key) {

        return Mono.defer(() -> {

            this.lock.lock();
            return this.repository.findNodeByKey(key)
                    .flatMap(node ->
                            this.repository.deleteNodeByKey(key)
                                    .then(Mono.just(node))
                    )
                    .doOnSuccess(node -> this.lock.unlock());

        })
                .subscribeOn(this.scheduler);

    }

}
