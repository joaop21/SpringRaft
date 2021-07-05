package com.springraft.persistencer2dbc.state;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends ReactiveCrudRepository<State,Long> {}
