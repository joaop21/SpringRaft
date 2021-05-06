package com.springRaft.reactive.persistence.log;

import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@Transactional
@AllArgsConstructor
public class LogService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* --------------------------------------------------- */

    /**
     * Method for getting the current log state from persistence mechanism.
     *
     * @return Mono<LogState> A mono with log state.
     * */
    public Mono<LogState> getState() {
        return this.logStateRepository.findById((long) 1);
    }

    /**
     * Method for inserting or updating the current persisted log state.
     *
     * @param logState New log state to insert/update.
     * @return Mono<LogState> New persisted log state.
     * */
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
    public Mono<Entry> getEntryByIndex(Long index) {
        return this.entryRepository.findById(index);
    }

    /**
     * Method that gets the index of the last stored entry in the log.
     *
     * @return Mono<Long> Long which is the index of the last entry in the log.
     * */
    public Mono<Long> getLastEntryIndex() {
        return this.entryRepository.findLastEntryIndex()
                .switchIfEmpty(Mono.just((long) 0));
    }

    /**
     * Method that gets the last entry in the log.
     *
     * @return Mono<Entry> Last entry in the log.
     * */
    public Mono<Entry> getLastEntry() {
        return this.entryRepository.findLastEntry()
                .switchIfEmpty(
                        Mono.just(new Entry((long) 0, (long) 0, null, false))
                );
    }

    /**
     * Method that inserts a contiguously new entry in the existing log.
     *
     * @param entry Entry to insert in the log.
     *
     * @return Mono<Entry> The new persisted entry.
     * */
    @Synchronized
    public Mono<Entry> insertEntry(Entry entry) {

        return this.getLastEntryIndex()
                .doOnNext(lastIndex -> entry.setIndex(lastIndex + 1))
                .flatMap(lastIndex -> this.entryRepository.save(entry));

    }

}
