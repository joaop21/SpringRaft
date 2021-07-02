package com.springRaft.reactive.persistence.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "R2DBC")
public interface R2DBCLogStateRepository extends ReactiveCrudRepository<LogState,Long> {}
