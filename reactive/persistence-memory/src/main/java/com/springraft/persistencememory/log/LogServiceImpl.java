package com.springraft.persistencememory.log;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class LogServiceImpl implements LogService {

    /* List with entries which represents the log */
    private final List<EntryImpl> log;

    /* Repository for Entry operations */
    private LogStateImpl logState;

    /* Sink for publish entry insertions */
    private final Sinks.Many<Mono<?>> insertionSink;

    /* --------------------------------------------------- */

    public LogServiceImpl() {
        this.log = new ArrayList<>();
        this.logState = null;
        this.insertionSink = Sinks.many().unicast().onBackpressureBuffer();

        this.insertionHandler().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<LogState> getState() {
        return Mono.create(sink -> sink.success(this.logState == null ? null : this.logState.clone()));
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
                .flatMap(this::saveState);
    }

    @Override
    public Mono<? extends LogState> saveState(Object logState) {
        return Mono.just(logState)
                .cast(LogStateImpl.class)
                .map(LogStateImpl::clone)
                .doOnNext(logState1 -> this.logState = logState1)
                .flatMap(logState1 -> Mono.just(this.logState.clone()));
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Entry> getEntryByIndex(Long index) {
        return Mono.create(sink -> {
            EntryImpl res = null;
            try {
                res = this.log.get(Math.toIntExact(index-1)).clone();
            } catch (IndexOutOfBoundsException ignored) {}
            sink.success(res);
        });
    }

    @Override
    public Mono<Long> getLastEntryIndex() {
        return Mono.just(this.log.size() == 0 ? 0 : this.log.get(this.log.size() - 1).getIndex());
    }

    @Override
    public Mono<? extends Entry> getLastEntry() {
        return Mono.just(this.log.size() == 0
                ? new EntryImpl((long) 0, (long) 0, null, false)
                : this.log.get(this.log.size() - 1).clone());
    }

    @Override
    public Flux<? extends Entry> getEntriesBetweenIndexes(Long minIndex, Long maxIndex) {
        return Mono.just(this.log.size())
                .flatMapMany(size -> maxIndex > size
                            ? this.clonedSublist(Math.toIntExact(minIndex - 1), size)
                            : this.clonedSublist(Math.toIntExact(minIndex - 1), Math.toIntExact(maxIndex-1)));
    }

    @Override
    public Mono<Integer> deleteIndexesGreaterThan(Long index) {
        return Mono.just(this.log.subList(Math.toIntExact(index), this.log.size()))
                .flatMap(list -> {
                    int size = list.size();
                    list.clear();
                    return Mono.just(size);
                });
    }

    @Override
    public Flux<? extends Entry> saveAllEntries(List<? extends Entry> entries) {
        return Flux.defer(() -> {
            for (Entry entry : entries)
                this.log.add(((EntryImpl) entry).clone());
            return this.clonedSublist(Math.toIntExact(entries.get(0).getIndex() - 1), this.log.size());
        });

    }

    /* --------------------------------------------------- */

    @Override
    public Mono<? extends Entry> insertEntry(Entry entry) {

        return Mono.just(Sinks.<EntryImpl>one())
                .doOnNext(responseSink -> {
                    while(this.insertionSink.tryEmitNext(insertEntryImpl(entry).doOnSuccess(responseSink::tryEmitValue)) != Sinks.EmitResult.OK);
                })
                .flatMap(Sinks.Empty::asMono);

    }

    /**
     * TODO
     * */
    private Flux<?> insertionHandler() {
        return this.insertionSink.asFlux().flatMap(mono -> mono,1);
    }

    /**
     * TODO
     * */
    private Mono<EntryImpl> insertEntryImpl(Entry entry) {
        return Mono.defer(this::getLastEntryIndex)
                .doOnNext(lastIndex -> ((EntryImpl)entry).setIndex(lastIndex + 1))
                .doOnNext(lastIndex -> this.log.add(((EntryImpl) entry).clone()))
                .flatMap(lastIndex -> Mono.just(this.log.get(this.log.size() - 1).clone()));
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    private Flux<EntryImpl> clonedSublist(int fromIndex, int toIndex) {
        return Flux.create(sink -> {
           for (int index = fromIndex ; index < toIndex ; index ++)
               sink.next(this.log.get(index).clone());
           sink.complete();
        });
    }

}

