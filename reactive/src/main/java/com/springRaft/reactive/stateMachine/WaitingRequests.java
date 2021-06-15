package com.springRaft.reactive.stateMachine;

import com.springRaft.reactive.util.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class WaitingRequests {

    /* Publisher of insertions of new client requests */
    private final Sinks.Many<Pair<Long,Sinks.One<Object>>> insertPublisher;

    /* Publisher of insertions of new client requests */
    private final Sinks.Many<Pair<Long,Object>> responsePublisher;

    /* Map that holds indexes and waiting rooms */
    private final Map<Long, Sinks.One<Object>> clientRequests;

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public WaitingRequests() {
        this.insertPublisher = Sinks.many().unicast().onBackpressureBuffer();
        this.responsePublisher = Sinks.many().unicast().onBackpressureBuffer();
        this.clientRequests = new HashMap<>();

        this.insertHandler().subscribe();
        this.responseHandler().subscribe();
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public Mono<Sinks.One<Object>> insertWaitingRequest(Long index) {

        return Mono.create(monoSink -> {

            Sinks.One<Object> sink = Sinks.one();
            Pair<Long,Sinks.One<Object>> pair = new Pair<>(index, sink);

            while (this.insertPublisher.tryEmitNext(pair) != Sinks.EmitResult.OK);

            monoSink.success(sink);
        });

    }

    /**
     * TODO
     * */
    private Flux<?> insertHandler() {

        return this.insertPublisher.asFlux()
                .flatMap(pair -> {

                    Sinks.One<?> previousSink = this.clientRequests.putIfAbsent(pair.first(), pair.second());

                    if (previousSink != null)
                        previousSink.tryEmitValue(null);

                    return Mono.empty();

                },1);

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    public Mono<Void> putResponse(Long index, Object response) {
        return Mono.just(this.responsePublisher.tryEmitNext(new Pair<>(index, response))).then();
    }

    /**
     * TODO
     * */
    private Flux<?> responseHandler() {

        return this.responsePublisher.asFlux()
                .flatMap(pair -> {

                    Sinks.One<Object> sink = this.clientRequests.remove(pair.first());

                    if (sink != null)
                        sink.tryEmitValue(pair.second());

                    return Mono.empty();

                },1);

    }

}
