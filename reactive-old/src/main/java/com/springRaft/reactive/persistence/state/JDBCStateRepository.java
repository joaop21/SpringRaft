package com.springRaft.reactive.persistence.state;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "JDBC")
public interface JDBCStateRepository extends JpaRepository<State,Long> {}
