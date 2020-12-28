package com.springraft.testexamples.reactivestack.repositories;

import com.springraft.testexamples.reactivestack.models.Counter;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CounterRepository extends ReactiveCrudRepository<Counter, Long> {
}
