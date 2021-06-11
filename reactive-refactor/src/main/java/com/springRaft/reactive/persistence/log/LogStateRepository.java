package com.springRaft.reactive.persistence.log;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LogStateRepository extends ReactiveCrudRepository<LogState,Long> {}
