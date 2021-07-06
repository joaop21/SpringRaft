package com.springraft.persistencejpa.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogStateRepository extends JpaRepository<LogStateImpl,Long> {}
