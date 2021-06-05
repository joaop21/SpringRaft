package com.springRaft.reactive.persistence.state;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StateRepository extends ReactiveCrudRepository<State,Long> {}
