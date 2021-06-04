package com.springRaft.testexamples.keyvaluestore.node;

import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("singleton")
@AllArgsConstructor
public class NodeService {

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    @Synchronized
    public Optional<Node> get(String key) {
        return this.repository.findByKey(key);
    }

    @Synchronized
    public List<Node> upsert(String key, String value) {

        List<Node> result = new ArrayList<>();

        Optional<Node> node = this.repository.findByKey(key);

        if (node.isPresent()) {
            this.repository.deleteNodeByKey(key);
            result.add(node.get());
        }

        Node savedNode = this.repository.save(new Node(key, value));
        result.add(savedNode);

        return result;

    }

    @Synchronized
    public Optional<Node> delete(String key) {

        Optional<Node> node = this.repository.findByKey(key);

        if (node.isPresent())
            this.repository.deleteNodeByKey(key);

        return node;

    }

}
