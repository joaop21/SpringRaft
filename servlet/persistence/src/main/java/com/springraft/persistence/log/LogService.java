package com.springraft.persistence.log;

import lombok.Synchronized;

import java.util.List;

public interface LogService {

    /**
     * Method for getting the current log state from persistence mechanism.
     *
     * @return LogState A mono with log state.
     * */
    LogState getState();

    /**
     * Method that sets the last applied field of the LogState and persist it.
     * */
    void incrementLastApplied();

    /**
     * Method for inserting or updating the current persisted log state.
     *
     * @param logState New log state to insert/update.
     * @return LogState New persisted log state.
     * */
    LogState saveState(LogState logState);

    /**
     * Method for getting an entry with a specific index.
     *
     * @param index Long which is the index of the entry in the log.
     *
     * @return Entry Entry found in that specific index.
     * */
    Entry getEntryByIndex(Long index);

    /**
     * Method that gets the index of the last stored entry in the log.
     *
     * @return Long Long which is the index of the last entry in the log.
     * */
    Long getLastEntryIndex();

    /**
     * Method that gets the last entry in the log.
     *
     * @return Entry Last entry in the log.
     * */
    Entry getLastEntry();

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return List of the Entries found.
     * */
    List<? extends Entry> getEntryBetweenIndex(Long minIndex, Long maxIndex);

    /**
     * Method that inserts a contiguously new entry in the existing log.
     *
     * @param entry Entry to insert in the log.
     *
     * @return Entry The new persisted entry.
     * */
    Entry insertEntry(Entry entry);

    /**
     * Method that deletes all the log entries with an index greater than a specific value.
     *
     * @param index Long that represents the index.
     * */
    void deleteIndexesGreaterThan(Long index);

    /**
     * Method that saves all the entries in a list.
     *
     * @param entries List of entries to save.
     *
     * @return List<Entry> Entries saved.
     * */
    List<? extends Entry> saveAllEntries(List<? extends Entry> entries);

}
