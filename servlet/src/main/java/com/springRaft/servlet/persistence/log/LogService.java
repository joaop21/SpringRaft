package com.springRaft.servlet.persistence.log;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogService {

    /* Repository for Entry operations */
    private final EntryRepository repository;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public Entry getIndex(Long index) {

        return this.repository
                .findById(index)
                .orElse(null);

    }

    /**
     * TODO
     * */
    public Entry insertEntry(Entry entry) {

        return this.repository.save(entry);

    }

    /**
     * TODO
     * */
    public void deleteIndex(Long index) {

        this.repository.deleteById(index);

    }

    /**
     * TODO
     * */
    public void deleteEntryByIndexGreaterThan(Long index) {

        this.repository.deleteEntryByIndexGreaterThan(index);

    }

}
