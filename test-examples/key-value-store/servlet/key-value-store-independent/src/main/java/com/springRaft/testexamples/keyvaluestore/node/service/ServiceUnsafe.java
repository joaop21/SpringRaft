package com.springRaft.testexamples.keyvaluestore.node.service;

import com.springRaft.testexamples.keyvaluestore.node.Node;
import com.springRaft.testexamples.keyvaluestore.node.NodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Unsafe")
@AllArgsConstructor
public class ServiceUnsafe implements NodeService {

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    @Override
    public Optional<Node> get(String key) {
        return this.repository.findByKey(key);
    }

    @Override
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

    @Override
    public Optional<Node> delete(String key) {

        Optional<Node> node = this.repository.findByKey(key);

        if (node.isPresent())
            this.repository.deleteNodeByKey(key);

        return node;

    }

}
