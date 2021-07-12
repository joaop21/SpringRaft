package com.springraft.persistence.log;

public interface LogState {

    LogState LogState(Long id, Long committedIndex, Long committedTerm, Long lastApplied);

    Long getId();
    Long getCommittedIndex();
    Long getCommittedTerm();
    Long getLastApplied();

    void setCommittedIndex(Long index);
    void setCommittedTerm(Long index);

}
