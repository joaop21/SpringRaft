package com.springraft.persistencememory.log;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import lombok.Synchronized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class LogServiceImpl implements LogService {

    /* List with entries which represents the log */
    private final List<EntryImpl> log = new ArrayList<>();

    private LogStateImpl logState = null;

    /* --------------------------------------------------- */

    @Override
    public LogState getState() {
        return this.logState;
    }

    @Override
    public void incrementLastApplied() {
        this.logState.setLastApplied(this.logState.getLastApplied() + 1);
    }

    @Override
    public LogState saveState(LogState logState) {
        this.logState = (LogStateImpl) logState;
        return this.logState;
    }

    /* --------------------------------------------------- */

    @Override
    public Entry getEntryByIndex(Long index) {
        try {
            return this.log.get(Math.toIntExact(index-1));
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    @Override
    public Long getLastEntryIndex() {
        return this.log.size() == 0
                ? 0
                : this.log.get(this.log.size() - 1).getIndex();
    }

    @Override
    public Entry getLastEntry() {
        return this.log.size() == 0
                ? new EntryImpl((long) 0, (long) 0, null)
                : this.log.get(this.log.size() - 1);
    }

    @Override
    public List<? extends Entry> getEntryBetweenIndex(Long minIndex, Long maxIndex) {
        int size = this.log.size();
        if (maxIndex >= size)
            return this.log.subList(Math.toIntExact(minIndex - 1), size);
        else return this.log.subList(Math.toIntExact(minIndex - 1), Math.toIntExact(maxIndex-1));
    }

    @Override
    @Synchronized
    public Entry insertEntry(Entry entry) {
        long lastEntryIndex = this.getLastEntryIndex();
        ((EntryImpl)entry).setIndex(lastEntryIndex+1);
        this.log.add((EntryImpl)entry);
        return this.log.get(this.log.size() - 1);
    }

    @Override
    public void deleteIndexesGreaterThan(Long index) {
        this.log.subList(Math.toIntExact(index), this.log.size()).clear();
    }

    @Override
    public List<? extends Entry> saveAllEntries(List<? extends Entry> entries) {
        for (Entry entry : entries)
            this.log.add((EntryImpl) entry);
        return this.log.subList(Math.toIntExact(entries.get(0).getIndex() - 1), this.log.size());
    }

}
