package com.springRaft.servlet.persistence.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogStateRepository extends JpaRepository<LogState,Long> {}
