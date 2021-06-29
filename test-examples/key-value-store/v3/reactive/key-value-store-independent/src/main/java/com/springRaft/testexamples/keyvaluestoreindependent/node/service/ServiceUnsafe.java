package com.springRaft.testexamples.keyvaluestoreindependent.node.service;

import com.springRaft.testexamples.keyvaluestoreindependent.node.Node;
import com.springRaft.testexamples.keyvaluestoreindependent.node.NodeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Unsafe")
public class ServiceUnsafe implements NodeService {

    private final NodeRepository repository;

    private final Scheduler jdbcScheduler;

    /* --------------------------------------------------- */

    public ServiceUnsafe(NodeRepository repository, @Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {
        this.repository = repository;
        this.jdbcScheduler = jdbcScheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Optional<Node>> get(String key) {
        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {

        AtomicReference<List<Node>> list = new AtomicReference<>(new ArrayList<>());

        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler)
                .filter(Optional::isPresent)
                    .flatMap(node ->
                            Mono.fromCallable(() -> repository.deleteNodeByKey(key))
                                    .subscribeOn(jdbcScheduler)
                                    .doOnSuccess(result -> list.get().add(node.get()))
                    )
                .then(
                        Mono.fromCallable(() -> this.repository.save(new Node(key,text.replaceFirst("value=", ""))))
                                .subscribeOn(jdbcScheduler)
                )
                .doOnNext(savedNode -> list.get().add(savedNode))
                .flatMap(savedNode -> Mono.defer(() -> Mono.just(list.get())));
    }

    @Override
    public Mono<Optional<Node>> delete(String key) {
        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler)
                .flatMap(node ->
                        Mono.fromCallable(() -> this.repository.deleteNodeByKey(key))
                                .subscribeOn(jdbcScheduler)
                                .then(Mono.just(node))
                );
    }
}
