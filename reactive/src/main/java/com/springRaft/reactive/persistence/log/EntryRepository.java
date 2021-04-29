package com.springRaft.reactive.persistence.log;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntryRepository extends ReactiveCrudRepository<Entry,Long> {

    /**
     * Select method for find the last entry's index in the log.
     *
     * @return Long Index of the last entry.
     * */
    @Query("SELECT MAX(entry.index) FROM Entry entry")
    Mono<Long> findLastEntryIndex();

    /**
     * Select method for getting the last entry in the log.
     *
     * @return Entry Last log entry.
     * */
    @Query("SELECT entry FROM Entry entry WHERE entry.index = (SELECT MAX(entry.index) FROM Entry entry)")
    Mono<Entry> findLastEntry();

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return List of the Entries found.
     * */
    @Query("SELECT entry FROM Entry entry WHERE entry.index >= ?1 AND entry.index < ?2")
    Flux<Entry> getNextEntries(Long minIndex, Long maxIndex);

    /**
     * Delete Method for deleting entries with an index greater than a specific number.
     *
     * @param index Long that represents the index from which the following will be deleted.
     * */
    Mono<Void> deleteEntryByIndexGreaterThan(Long index);

}
