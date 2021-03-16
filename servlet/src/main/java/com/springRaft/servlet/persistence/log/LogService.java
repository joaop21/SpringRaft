package com.springRaft.servlet.persistence.log;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
    public Entry getEntryByIndex(Long index) {

        return this.entryRepository
                .findById(index)
                .orElse(null);

    }

    /**
     * TODO
     * */
    public Long getLastEntryIndex() {

        Long index = this.entryRepository.findLastEntryIndex();
        return index == null ? (long) 0 : index;

    }

    /**
     * TODO
     * */
    public Entry getLastEntry() {

        Entry entry = this.entryRepository.findLastEntry();
        return entry == null
                ? new Entry((long) 0, (long) 0, null)
                : entry;

    }

    /**
     * TODO
     * */
    public Entry insertEntry(Entry entry) {

        Long lastIndex = this.getLastEntryIndex();
        entry.setIndex(lastIndex + 1);
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
    public void deleteIndexesGreaterThan(Long index) {

        this.entryRepository.deleteEntryByIndexGreaterThan(index);

    }

    /**
     * TODO
     */
    public LogState saveState(LogState logState) {

        return this.logStateRepository.save(logState);

    }

}
