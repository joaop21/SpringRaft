package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Unsafe")
@AllArgsConstructor
public class ServiceUnsafe implements NodeService {

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {
        return this.repository.findNodeByKey(key);
    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {

        AtomicReference<List<Node>> list = new AtomicReference<>(new ArrayList<>());

        return this.repository.findNodeByKey(key)
                .flatMap(node ->
                        repository.deleteNodeByKey(key)
                                .doOnSuccess(result -> list.get().add(node))
                )
                .then(this.repository.save(new Node(key,text.replaceFirst("value=", ""))))
                .doOnNext(savedNode -> list.get().add(savedNode))
                .flatMap(savedNode -> Mono.defer(() -> Mono.just(list.get())));
    }

    @Override
    public Mono<Node> delete(String key) {
        return this.repository.findNodeByKey(key)
                .flatMap(node ->
                        this.repository.deleteNodeByKey(key)
                                .then(Mono.just(node))
                );
    }

}
