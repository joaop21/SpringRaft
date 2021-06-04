package com.springRaft.testexamples.reactivekeyvaluestore.node;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Transactional
public interface NodeRepository extends ReactiveCrudRepository<Node,Long> {

    Mono<Node> findNodeByKey(String key);

    Mono<Void> deleteNodeByKey(String key);

}
