package com.springraft.testexamples.servletstack.repositories;

import com.springraft.testexamples.servletstack.models.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {}
