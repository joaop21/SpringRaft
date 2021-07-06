package com.springraft.persistence.log;

public interface LogState {

    LogState LogState(Long id, Long committedIndex, Long committedTerm, Long lastApplied, boolean isNew);

}