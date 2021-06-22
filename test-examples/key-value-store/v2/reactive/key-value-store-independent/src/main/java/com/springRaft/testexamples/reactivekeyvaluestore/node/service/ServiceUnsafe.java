package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Unsafe")
@AllArgsConstructor
public class ServiceUnsafe implements NodeService {

    private final Map<String,Node> keyValueStore = new HashMap<>();

    private final AtomicLong index = new AtomicLong(0);

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {
        return Mono.fromSupplier(() -> this.keyValueStore.get(key));
    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {

        return Mono.fromSupplier(() -> {
            List<Node> result = new ArrayList<>();
            Node newNode = new Node(index.incrementAndGet(), key, text.replaceFirst("value=", ""));
            Optional.ofNullable(this.keyValueStore.put(key, newNode)).ifPresent(result::add);
            result.add(newNode);
            return result;
        });
    }

    @Override
    public Mono<Node> delete(String key) {
        return Mono.fromSupplier(() -> this.keyValueStore.remove(key));
    }

}
