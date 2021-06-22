package com.springRaft.testexamples.keyvaluestore.node;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByKey(String key);

    void deleteNodeByKey(String key);

}
