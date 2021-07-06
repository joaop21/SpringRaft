package com.springraft.persistencer2dbc.log;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogStateRepository extends ReactiveCrudRepository<LogStateImpl,Long> {}
