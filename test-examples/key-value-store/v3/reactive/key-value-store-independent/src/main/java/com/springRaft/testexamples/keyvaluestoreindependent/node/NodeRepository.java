package com.springRaft.testexamples.keyvaluestoreindependent.node;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findNodeByKey(String key);

    Integer deleteNodeByKey(String key);

}
