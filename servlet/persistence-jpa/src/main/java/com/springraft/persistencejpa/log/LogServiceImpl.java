package com.springraft.persistencejpa.log;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
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
public class LogServiceImpl implements LogService {

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* --------------------------------------------------- */

    @Override
    public LogState getState() {
        return this.logStateRepository
                .findById((long) 1)
                .orElse(null);
    }

    @Override
    public void incrementLastApplied() {
        LogState logState = this.getState();
        ((LogStateImpl)logState).setLastApplied(logState.getLastApplied() + 1);
        this.logStateRepository.save((LogStateImpl)logState);
    }

    @Override
    public LogState saveState(LogState logState) {
        return this.logStateRepository.save((LogStateImpl)logState);
    }

    /* --------------------------------------------------- */

    @Override
    public Entry getEntryByIndex(Long index) {
        return this.entryRepository
                .findById(index)
                .orElse(null);
    }

    @Override
    public Long getLastEntryIndex() {
        Long index = this.entryRepository.findLastEntryIndex();
        return index == null ? (long) 0 : index;
    }

    @Override
    public Entry getLastEntry() {
        Entry entry = this.entryRepository.findLastEntry();
        return entry == null
                ? new EntryImpl((long) 0, (long) 0, null)
                : entry;
    }

    @Override
    public List<? extends Entry> getEntryBetweenIndex(Long minIndex, Long maxIndex) {
        return this.entryRepository.getNextEntries(minIndex, maxIndex);
    }

    @Override
    @Synchronized
    public Entry insertEntry(Entry entry) {
        Long lastIndex = this.getLastEntryIndex();
        ((EntryImpl)entry).setIndex(lastIndex + 1);
        return this.entryRepository.save((EntryImpl)entry);
    }

    @Override
    public void deleteIndexesGreaterThan(Long index) {
        this.entryRepository.deleteEntryImplByIndexGreaterThan(index);
    }

    @Override
    public List<? extends Entry> saveAllEntries(List<? extends Entry> entries) {
        return this.entryRepository.saveAll((List<EntryImpl>)entries);
    }
}
