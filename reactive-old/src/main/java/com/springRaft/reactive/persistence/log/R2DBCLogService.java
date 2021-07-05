package com.springRaft.reactive.persistence.log;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "raft.database-connectivity", havingValue = "R2DBC")
@AllArgsConstructor
public class R2DBCLogService implements LogService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(R2DBCLogService.class);

    /* Repository for Entry operations */
    private final R2DBCEntryRepository entryRepository;

    /* Repository for Entry operations */
    private final R2DBCLogStateRepository logStateRepository;

    /* Mutex for some operations */
    private final Lock lock = new ReentrantLock();

    /* --------------------------------------------------- */

    @Override
    public Mono<LogState> getState() {
        return this.logStateRepository.findById((long) 1);
    }

    @Override
    public Mono<LogState> incrementLastApplied() {
        return this.getState()
                .map(logState -> {
                    logState.setLastApplied(logState.getLastApplied() + 1);
                    logState.setNew(false);
                    return logState;
                })
                .flatMap(this.logStateRepository::save);
    }

    @Override
    public Mono<LogState> saveState(LogState logState) {
        return this.logStateRepository.save(logState)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Entry> getEntryByIndex(Long index) {
        return this.entryRepository.findById(index);
    }

    @Override
    public Mono<Long> getLastEntryIndex() {
        return this.entryRepository.findLastEntryIndex()
                .switchIfEmpty(Mono.just((long) 0));
    }

    @Override
    public Mono<Entry> getLastEntry() {
        return this.entryRepository.findLastEntry()
                .switchIfEmpty(Mono.just(new Entry((long) 0, (long) 0, null, false)));
    }

    @Override
    public Flux<Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex) {
        return this.entryRepository.getNextEntries(minIndex, maxIndex);
    }

    @Override
    public Mono<Entry> insertEntry(Entry entry) {

        return Mono.<Entry>defer(() -> {
            lock.lock();
            return this.getLastEntryIndex()
                    .doOnNext(lastIndex -> entry.setIndex(lastIndex + 1))
                    .flatMap(lastIndex -> this.entryRepository.save(entry))
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
    public Flux<Entry> saveAllEntries(List<Entry> entries) {
        return this.entryRepository.saveAll(entries);
    }

}
