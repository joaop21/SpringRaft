package com.springraft.persistence.log;

public interface Entry {

    Entry Entry(long index, long term, String command, boolean isNew);

}
