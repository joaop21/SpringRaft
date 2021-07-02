package com.springRaft.reactive.persistence.state;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "R2DBC")
public interface R2DBCStateRepository extends ReactiveCrudRepository<State,Long> {}
