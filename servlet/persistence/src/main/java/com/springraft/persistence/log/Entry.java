package com.springraft.persistence.log;

public interface Entry {

    Entry Entry(long index, long term, String command);

    Long getIndex();
    Long getTerm();
    String getCommand();

}
