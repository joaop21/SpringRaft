package com.springraft.persistencejpa.log;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope("singleton")
@Transactional
public class LogServiceImpl implements LogService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* Mutex for some operations */
    private final Lock lock = new ReentrantLock();

    /* Scheduler to execute database operations */
    private final Scheduler scheduler;

    /* --------------------------------------------------- */

    public LogServiceImpl(
            EntryRepository entryRepository,
            LogStateRepository logStateRepository,
            @Qualifier("jpaScheduler") Scheduler jpaScheduler
    ) {
        this.entryRepository = entryRepository;
        this.logStateRepository = logStateRepository;
        this.scheduler = jpaScheduler;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<LogState> getState() {
        return Mono.fromCallable(() -> this.logStateRepository.findById((long) 1))
                .subscribeOn(this.scheduler)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }

    @Override
    public Mono<? extends LogState> incrementLastApplied() {
        return this.getState()
                .cast(LogStateImpl.class)
                .map(logState -> {
                    logState.setLastApplied(logState.getLastApplied() + 1);
                    return logState;
                })
                .flatMap(logState ->
                        Mono.fromCallable(() -> this.logStateRepository.save(logState))
                                .subscribeOn(this.scheduler)
                );
    }

    @Override
    public Mono<? extends LogState> saveState(Object logState) {
        return Mono.fromCallable(() -> this.logStateRepository.save((LogStateImpl) logState))
                .subscribeOn(this.scheduler)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Entry> getEntryByIndex(Long index) {
        return Mono.fromCallable(() -> this.entryRepository.findById(index))
                .subscribeOn(this.scheduler)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty));
    }

    @Override
    public Mono<Long> getLastEntryIndex() {
        return Mono.fromCallable(this.entryRepository::findLastEntryIndex)
                .subscribeOn(this.scheduler)
                .switchIfEmpty(Mono.just((long) 0));
    }

    @Override
    public Mono<? extends Entry> getLastEntry() {
        return Mono.fromCallable(this.entryRepository::findLastEntry)
                .subscribeOn(this.scheduler)
                .switchIfEmpty(Mono.just(new EntryImpl((long) 0, (long) 0, null, false)));
    }

    @Override
    public Flux<Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex) {
        return Mono.fromCallable(() -> this.entryRepository.getNextEntries(minIndex, maxIndex))
                .subscribeOn(this.scheduler)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<? extends Entry> insertEntry(Entry entry) {

        return Mono.<Entry>defer(() -> {
            lock.lock();
            return this.getLastEntryIndex()
                    .doOnNext(lastIndex -> ((EntryImpl)entry).setIndex(lastIndex + 1))
                    .flatMap(lastIndex ->
                            Mono.fromCallable(() -> this.entryRepository.save((EntryImpl) entry))
                                    .subscribeOn(this.scheduler)
                    )
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
        return Mono.fromCallable(() -> this.entryRepository.deleteEntryByIndexGreaterThan(index))
                .subscribeOn(this.scheduler);
    }

    @Override
    public Flux<? extends Entry> saveAllEntries(List<? extends Entry> entries) {
        return Mono.fromCallable(() -> this.entryRepository.saveAll((List<EntryImpl>)entries))
                .subscribeOn(this.scheduler)
                .flatMapMany(Flux::fromIterable);
    }

}
