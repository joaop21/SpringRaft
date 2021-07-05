package com.springraft.persistencer2dbc.log;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LogStateRepository extends ReactiveCrudRepository<LogState,Long> {}
