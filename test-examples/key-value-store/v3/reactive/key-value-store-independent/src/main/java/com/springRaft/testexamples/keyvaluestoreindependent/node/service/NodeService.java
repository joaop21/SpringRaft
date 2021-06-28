package com.springRaft.testexamples.keyvaluestoreindependent.node.service;

import com.springRaft.testexamples.keyvaluestoreindependent.node.Node;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface NodeService {

    Mono<Optional<Node>> get(String key);

    Mono<List<Node>> upsert(String key, String text);

    Mono<Optional<Node>> delete(String key);

}
