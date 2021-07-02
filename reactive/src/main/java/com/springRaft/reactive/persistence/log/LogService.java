package com.springRaft.reactive.persistence.log;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LogService {

    Mono<LogState> getState();
    Mono<LogState> incrementLastApplied();
    Mono<LogState> saveState(LogState logState);

    Mono<Entry> getEntryByIndex(Long index);
    Mono<Long> getLastEntryIndex();
    Mono<Entry> getLastEntry();
    Flux<Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex);
    Mono<Entry> insertEntry(Entry entry);
    Mono<Integer> deleteIndexesGreaterThan(Long index);
    Flux<Entry> saveAllEntries(List<Entry> entries);

}
