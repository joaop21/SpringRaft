package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Scope("singleton")
@AllArgsConstructor
public class ServiceUnsafe {

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    public Mono<Node> get(String key) {
        return this.repository.findNodeByKey(key);
    }

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

    public Mono<Node> delete(String key) {
        return this.repository.findNodeByKey(key)
                .flatMap(node ->
                        this.repository.deleteNodeByKey(key)
                                .then(Mono.just(node))
                );
    }

}
