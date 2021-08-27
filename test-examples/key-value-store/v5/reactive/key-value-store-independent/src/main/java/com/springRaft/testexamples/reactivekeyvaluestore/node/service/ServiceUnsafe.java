package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Unsafe")
public class ServiceUnsafe implements NodeService {

    private final NodeRepository repository;

    private final Scheduler repoScheduler;

    /* --------------------------------------------------- */

    public ServiceUnsafe(NodeRepository repository, @Qualifier("repoScheduler") Scheduler repoScheduler) {
        this.repository = repository;
        this.repoScheduler = repoScheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {
        return this.repository.findNodeByKey(key)
                .subscribeOn(this.repoScheduler);
    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {

        AtomicReference<List<Node>> list = new AtomicReference<>(new ArrayList<>());

        return this.repository.findNodeByKey(key)
                .subscribeOn(this.repoScheduler)
                .flatMap(node ->
                        repository.deleteNodeByKey(key)
                                .subscribeOn(this.repoScheduler)
                                .doOnSuccess(result -> list.get().add(node))
                )
                .then(
                        this.repository.save(new Node(key,text.replaceFirst("value=", "")))
                                .subscribeOn(this.repoScheduler)
                )
                .doOnNext(savedNode -> list.get().add(savedNode))
                .flatMap(savedNode -> Mono.defer(() -> Mono.just(list.get())));
    }

    @Override
    public Mono<Node> delete(String key) {
        return this.repository.findNodeByKey(key)
                .subscribeOn(this.repoScheduler)
                .flatMap(node ->
                        this.repository.deleteNodeByKey(key)
                                .subscribeOn(this.repoScheduler)
                                .then(Mono.just(node))
                );
    }

}
