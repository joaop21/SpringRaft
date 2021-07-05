package com.springraft.persistencer2dbc.state;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface StateRepository extends ReactiveCrudRepository<State,Long> {}
