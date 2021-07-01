package com.springRaft.servlet.persistence.log;

import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Scope("singleton")
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
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return List of the Entries found.
     * */
    public List<Entry> getEntryBetweenIndex(Long minIndex, Long maxIndex) {

        return this.entryRepository.getNextEntries(minIndex, maxIndex);

    }

    /**
     * TODO
     * */
    @Synchronized
    public Entry insertEntry(Entry entry) {

        Long lastIndex = this.getLastEntryIndex();
        entry.setIndex(lastIndex + 1);
        return this.entryRepository.save(entry);

    }

    /**
     * TODO
     * */
    @Synchronized
    public List<Entry> saveAllEntries(List<Entry> entries) {

        return this.entryRepository.saveAll(entries);

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
