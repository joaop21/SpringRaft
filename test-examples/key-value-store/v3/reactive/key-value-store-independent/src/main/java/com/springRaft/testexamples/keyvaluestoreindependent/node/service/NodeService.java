package com.springRaft.testexamples.keyvaluestoreindependent.node.service;

import com.springRaft.testexamples.keyvaluestoreindependent.node.Node;
import reactor.core.publisher.Mono;

import java.util.List;

public interface NodeService {

    Mono<Node> get(String key);

    Mono<List<Node>> upsert(String key, String text);

    Mono<Node> delete(String key);

}
