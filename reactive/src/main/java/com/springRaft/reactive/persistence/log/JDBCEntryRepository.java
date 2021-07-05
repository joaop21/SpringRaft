package com.springRaft.reactive.persistence.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "JDBC")
public interface JDBCEntryRepository extends JpaRepository<Entry,Long> {

    /**
     * Select method for find the last entry's index in the log.
     *
     * @return Long Index of the last entry.
     * */
    @Query("SELECT MAX(entry.index) FROM Entry entry")
    Long findLastEntryIndex();

    /**
     * Select method for getting the last entry in the log.
     *
     * @return Entry Last log entry.
     * */
    @Query("SELECT entry FROM Entry entry WHERE entry.index = (SELECT MAX(entry.index) FROM Entry entry)")
    Entry findLastEntry();

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return List of the Entries found.
     * */
    @Query("SELECT entry FROM Entry entry WHERE entry.index >= ?1 AND entry.index < ?2")
    List<Entry> getNextEntries(Long minIndex, Long maxIndex);

    /**
     * Delete Method for deleting entries with an index greater than a specific number.
     *
     * @param index Long that represents the index from which the following will be deleted.
     *
     * @return Integer which represents the number of deleted entries.
     * */
    Integer deleteEntryByIndexGreaterThan(Long index);

}
