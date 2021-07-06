package com.springraft.persistence.log;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LogService {

    /**
     * Method for getting the current log state from persistence mechanism.
     *
     * @return Mono<LogState> A mono with log state.
     * */
    Mono<? extends LogState> getState();

    /**
     * Method that sets the last applied field of the LogState and persist it.
     *
     * @return The updated LogState.
     * */
    Mono<? extends LogState> incrementLastApplied();

    /**
     * Method for inserting or updating the current persisted log state.
     *
     * @param logState New log state to insert/update.
     * @return Mono<LogState> New persisted log state.
     * */
    Mono<? extends LogState> saveState(Object logState);


    /**
     * Method for getting an entry with a specific index.
     *
     * @param index Long which is the index of the entry in the log.
     *
     * @return Mono<Entry> Entry found in that specific index.
     * */
    Mono<? extends Entry> getEntryByIndex(Long index);

    /**
     * Method that gets the index of the last stored entry in the log.
     *
     * @return Mono<Long> Long which is the index of the last entry in the log.
     * */
    Mono<? extends Long> getLastEntryIndex();

    /**
     * Method that gets the last entry in the log.
     *
     * @return Mono<Entry> Last entry in the log.
     * */
    Mono<? extends Entry> getLastEntry();

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return Flux of the Entries found.
     * */
    Flux<? extends Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex);

    /**
     * Method that inserts a contiguously new entry in the existing log.
     *
     * @param entry Entry to insert in the log.
     *
     * @return Mono<Entry> The new persisted entry.
     * */
    Mono<? extends Entry> insertEntry(Entry entry);

    /**
     * Method that deletes all the log entries with an index greater than a specific value.
     *
     * @param index Long that represents the index.
     *
     * @return Integer that represents the elements deleted.
     * */
    Mono<Integer> deleteIndexesGreaterThan(Long index);

    /**
     * Method that saves all the entries in a list.
     *
     * @param entries List of entries to save.
     *
     * @return Flux<Entry> Entries saved.
     * */
    Flux<? extends Entry> saveAllEntries(List<? extends Entry> entries);

}
