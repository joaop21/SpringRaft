package com.springRaft.servlet.persistence.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry,Long> {

    /**
     * Delete Method for deleting entries with an index greater than a specific number.
     *
     * @param index Long that represents the index from which the following will be deleted.
     *
     * @return int Number of deleted entries.
     * */
    int deleteEntryByIndexGreaterThan(Long index);

}
