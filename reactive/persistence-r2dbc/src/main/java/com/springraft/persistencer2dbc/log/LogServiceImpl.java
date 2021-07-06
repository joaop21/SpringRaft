package com.springraft.persistencer2dbc.log;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope("singleton")
@Transactional
@AllArgsConstructor
public class LogServiceImpl implements LogService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* Mutex for some operations */
    private final Lock lock = new ReentrantLock();

    /* --------------------------------------------------- */

    @Override
    public Mono<LogState> getState() {
        return this.logStateRepository.findById((long) 1)
                .cast(LogState.class);
    }

    @Override
    public Mono<LogState> incrementLastApplied() {
        return this.getState()
                .cast(LogStateImpl.class)
                .map(logState -> {
                    logState.setLastApplied(logState.getLastApplied() + 1);
                    logState.setNew(false);
                    return logState;
                })
                .flatMap(this.logStateRepository::save);
    }

    @Override
    public Mono<? extends LogState> saveState(Object logState) {
        return Mono.just(logState)
                .cast(LogStateImpl.class)
                .flatMap(this.logStateRepository::save)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<? extends Entry> getEntryByIndex(Long index) {
        return this.entryRepository.findById(index);
    }

    @Override
    public Mono<Long> getLastEntryIndex() {
        return this.entryRepository.findLastEntryIndex()
                .switchIfEmpty(Mono.just((long) 0));
    }

    @Override
    public Mono<? extends Entry> getLastEntry() {
        return this.entryRepository.findLastEntry()
                .switchIfEmpty(Mono.just(new EntryImpl((long) 0, (long) 0, null, false)));
    }

    @Override
    public Flux<? extends Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex) {
        return this.entryRepository.getNextEntries(minIndex, maxIndex);
    }

    @Override
    public Mono<? extends Entry> insertEntry(Entry entry) {

        return Mono.<Entry>defer(() -> {
            lock.lock();
            return this.getLastEntryIndex()
                    .doOnNext(lastIndex -> ((EntryImpl)entry).setIndex(lastIndex + 1))
                    .flatMap(lastIndex -> this.entryRepository.save(((EntryImpl)entry)))
                    .flatMap(savedEntry ->
                            Mono.create(monoSink -> {
                                lock.unlock();
                                monoSink.success(savedEntry);
                            })
                    );
        })
                .onErrorResume(DataIntegrityViolationException.class, error -> this.insertEntry(entry));

    }

    @Override
    public Mono<Integer> deleteIndexesGreaterThan(Long index) {
        return this.entryRepository.deleteEntryByIndexGreaterThan(index);
    }

    @Override
    public Flux<? extends Entry> saveAllEntries(List<? extends Entry> entries) {
        return this.entryRepository.saveAll((List<EntryImpl>) entries);
    }

}
