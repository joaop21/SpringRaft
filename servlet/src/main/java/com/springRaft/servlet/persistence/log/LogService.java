package com.springRaft.servlet.persistence.log;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogService {

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public LogState getState() {

        return this.logStateRepository
                .findById((long) 1)
                .orElse(null);

    }

    /**
     * TODO
     * */
    public Entry getIndex(Long index) {

        return this.entryRepository
                .findById(index)
                .orElse(null);

    }

    /**
     * TODO
     * */
    public Long getLastEntry() {

        return this.entryRepository.findLastEntry();

    }

    /**
     * TODO
     * */
    public Entry insertEntry(Entry entry) {

        return this.entryRepository.save(entry);

    }

    /**
     * TODO
     * */
    public void incrementCommittedIndex() {
        LogState logState = this.getState();
        logState.setCommittedIndex(logState.getCommittedIndex() + 1);
        this.logStateRepository.save(logState);
    }

    /**
     * TODO
     * */
    public void incrementCommittedTerm() {
        LogState logState = this.getState();
        logState.setCommittedTerm(logState.getCommittedTerm() + 1);
        this.logStateRepository.save(logState);
    }

    /**
     * TODO
     * */
    public void incrementLastApplied() {
        LogState logState = this.getState();
        logState.setLastApplied(logState.getLastApplied() + 1);
        this.logStateRepository.save(logState);
    }

    /**
     * TODO
     * */
    public void deleteIndex(Long index) {

        this.entryRepository.deleteById(index);

    }

    /**
     * TODO
     * */
    public Integer deleteIndexesGreaterThan(Long index) {

        return this.entryRepository.deleteEntryByIndexGreaterThan(index);

    }

    /**
     * TODO
     */
    public LogState saveState(LogState logState) {

        return this.logStateRepository.save(logState);

    }

}
