package com.springRaft.testexamples.servletfaulttolerantcounter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter,Long> {}
