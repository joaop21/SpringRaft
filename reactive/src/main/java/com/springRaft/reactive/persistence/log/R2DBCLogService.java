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

    /**
     * Method for getting the current log state from persistence mechanism.
     *
     * @return Mono<LogState> A mono with log state.
     * */
    @Override
    public Mono<LogState> getState() {
        return this.logStateRepository.findById((long) 1);
    }

    /**
     * Method that sets the last applied field of the LogState and persist it.
     *
     * @return The updated LogState.
     * */
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

    /**
     * Method for inserting or updating the current persisted log state.
     *
     * @param logState New log state to insert/update.
     * @return Mono<LogState> New persisted log state.
     * */
    @Override
    public Mono<LogState> saveState(LogState logState) {
        return this.logStateRepository.save(logState)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    /**
     * Method for getting an entry with a specific index.
     *
     * @param index Long which is the index of the entry in the log.
     *
     * @return Mono<Entry> Entry found in that specific index.
     * */
    @Override
    public Mono<Entry> getEntryByIndex(Long index) {
        return this.entryRepository.findById(index);
    }

    /**
     * Method that gets the index of the last stored entry in the log.
     *
     * @return Mono<Long> Long which is the index of the last entry in the log.
     * */
    @Override
    public Mono<Long> getLastEntryIndex() {
        return this.entryRepository.findLastEntryIndex()
                .switchIfEmpty(Mono.just((long) 0));
    }

    /**
     * Method that gets the last entry in the log.
     *
     * @return Mono<Entry> Last entry in the log.
     * */
    @Override
    public Mono<Entry> getLastEntry() {
        return this.entryRepository.findLastEntry()
                .switchIfEmpty(Mono.just(new Entry((long) 0, (long) 0, null, false)));
    }

    /**
     * Method that gets the entries between two indexes.
     *
     * @param minIndex Index to begin the search.
     * @param maxIndex Index to stop the search.
     *
     * @return Flux of the Entries found.
     * */
    @Override
    public Flux<Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex) {
        return this.entryRepository.getNextEntries(minIndex, maxIndex);
    }

    /**
     * Method that inserts a contiguously new entry in the existing log.
     *
     * @param entry Entry to insert in the log.
     *
     * @return Mono<Entry> The new persisted entry.
     * */
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

    /**
     * Method that deletes all the log entries with an index greater than a specific value.
     *
     * @param index Long that represents the index.
     *
     * @return Integer that represents the elements deleted.
     * */
    @Override
    public Mono<Integer> deleteIndexesGreaterThan(Long index) {
        return this.entryRepository.deleteEntryByIndexGreaterThan(index);
    }

    /**
     * Method that saves all the entries in a list.
     *
     * @param entries List of entries to save.
     *
     * @return Flux<Entry> Entries saved.
     * */
    @Override
    public Flux<Entry> saveAllEntries(List<Entry> entries) {
        return this.entryRepository.saveAll(entries);
    }

}
