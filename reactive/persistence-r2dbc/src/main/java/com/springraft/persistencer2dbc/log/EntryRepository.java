package com.springraft.persistencer2dbc.log;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EntryRepository extends ReactiveCrudRepository<EntryImpl,Long> {

    /**
     * Select method for find the last entry's index in the log.
     *
     * @return Long Index of the last entry.
     * */
    @Query("SELECT MAX(entry.index) FROM EntryImpl entry")
    Mono<Long> findLastEntryIndex();

    /**
     * Select method for getting the last entry in the log.
     *
     * @return Entry Last log entry.
     * */
    @Query("SELECT * FROM EntryImpl entry WHERE entry.index = (SELECT MAX(entry.index) FROM EntryImpl entry)")
    Mono<EntryImpl> findLastEntry();

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return List of the Entries found.
     * */
    @Query("SELECT * FROM EntryImpl entry WHERE entry.index >= ?1 AND entry.index < ?2")
    Flux<EntryImpl> getNextEntries(Long minIndex, Long maxIndex);

    /**
     * Delete Method for deleting entries with an index greater than a specific number.
     *
     * @param index Long that represents the index from which the following will be deleted.
     *
     * @return Integer that represents the elements deleted.
     * */
    Mono<Integer> deleteEntryByIndexGreaterThan(Long index);

}
