package com.springRaft.testexamples.keyvaluestore.node.service;

import com.springRaft.testexamples.keyvaluestore.node.Node;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Sync")
public class ServiceSync implements NodeService {

    private final Map<String,Node> keyValueStore = new HashMap<>();

    private final AtomicLong index = new AtomicLong(0);

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public Optional<Node> get(String key) {
        return Optional.ofNullable(this.keyValueStore.get(key));
    }

    @Override
    @Synchronized
    public List<Node> upsert(String key, String value) {

        List<Node> result = new ArrayList<>();

        Node newNode = new Node(index.incrementAndGet(), key, value);

        Optional.ofNullable(this.keyValueStore.put(key, newNode)).ifPresent(result::add);

        result.add(newNode);

        return result;

    }

    @Override
    @Synchronized
    public Optional<Node> delete(String key) {
        return Optional.ofNullable(this.keyValueStore.remove(key));
    }

    /* --------------------------------------------------- */

}
