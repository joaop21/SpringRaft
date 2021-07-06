package com.springraft.persistence.log;

public interface LogState {

    LogState LogState(Long id, Long committedIndex, Long committedTerm, Long lastApplied, boolean isNew);

    Long getId();
    Long getCommittedIndex();
    Long getCommittedTerm();
    Long getLastApplied();
    boolean isNew();

    void setCommittedIndex(Long index);
    void setCommittedTerm(Long index);
    void setNew(boolean isNew);
}