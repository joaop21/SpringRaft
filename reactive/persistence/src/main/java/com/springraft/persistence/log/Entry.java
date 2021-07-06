package com.springraft.persistence.log;

public interface Entry {

    Entry Entry(long index, long term, String command, boolean isNew);

    Long getIndex();
    Long getTerm();
    String getCommand();
    boolean isNew();

    void setNew(boolean isNew);

}
