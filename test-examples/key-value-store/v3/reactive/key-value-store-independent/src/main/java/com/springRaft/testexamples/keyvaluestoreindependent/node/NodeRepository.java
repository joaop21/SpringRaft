package com.springRaft.testexamples.keyvaluestoreindependent.node;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByKey(String key);

    void deleteNodeByKey(String key);

}
