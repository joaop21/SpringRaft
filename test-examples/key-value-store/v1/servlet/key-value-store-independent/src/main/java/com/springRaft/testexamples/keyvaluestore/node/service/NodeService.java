package com.springRaft.testexamples.keyvaluestore.node.service;

import com.springRaft.testexamples.keyvaluestore.node.Node;

import java.util.List;
import java.util.Optional;

public interface NodeService {

    Optional<Node> get(String key);

    List<Node> upsert(String key, String text);

    Optional<Node> delete(String key);

}
