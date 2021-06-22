package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Publisher")
public class ServicePublisher implements NodeService {

    private final Map<String,Node> keyValueStore = new HashMap<>();

    private final AtomicLong index = new AtomicLong(0);

    private final Sinks.Many<Mono<?>> sink = Sinks.many().multicast().onBackpressureBuffer();

    /* --------------------------------------------------- */

    public ServicePublisher() {
        this.servicePublisher().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {

        return Mono.<Sinks.One<Node>>create(oneMonoSink -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            while(this.sink.tryEmitNext(this.safeGet(key, sinkNode)) != Sinks.EmitResult.OK);
            oneMonoSink.success(sinkNode);
        })
                .flatMap(Sinks.Empty::asMono);

    }

    @Override
    public Mono<List<Node>> upsert(String key, String text) {
        return Mono.<Sinks.One<List<Node>>>create(manyMonoSink -> {
            Sinks.One<List<Node>> sinkNode = Sinks.one();
            while(this.sink.tryEmitNext(this.safeUpsert(key, text, sinkNode)) != Sinks.EmitResult.OK);
            manyMonoSink.success(sinkNode);
        })
                .flatMap(Sinks.Empty::asMono);
    }

    @Override
    public Mono<Node> delete(String key) {
        return Mono.<Sinks.One<Node>>create(nodeMonoSink -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            while(this.sink.tryEmitNext(this.safeDelete(key, sinkNode)) != Sinks.EmitResult.OK);
            nodeMonoSink.success(sinkNode);
        })
                .flatMap(Sinks.Empty::asMono);
    }

    /* --------------------------------------------------- */

    public Flux<?> servicePublisher() {
        return this.sink.asFlux().flatMap(mono -> mono, 1);
    }

    /* --------------------------------------------------- */

    private Mono<Node> safeGet(String key, Sinks.One<Node> sinkOne) {
        return Mono.fromSupplier(() -> this.keyValueStore.get(key))
                .doOnSuccess(sinkOne::tryEmitValue);
    }

    private Mono<List<Node>> safeUpsert(String key, String text, Sinks.One<List<Node>> sinkOne) {
        return Mono.fromSupplier(() -> {
            List<Node> result = new ArrayList<>();
            Node newNode = new Node(index.incrementAndGet(), key, text.replaceFirst("value=", ""));
            Optional.ofNullable(this.keyValueStore.put(key, newNode)).ifPresent(result::add);
            result.add(newNode);
            return result;
        })
                .doOnSuccess(sinkOne::tryEmitValue);
    }

    private Mono<Node> safeDelete(String key, Sinks.One<Node> sinkNode) {

        return Mono.fromSupplier(() -> this.keyValueStore.remove(key))
                .doOnSuccess(sinkNode::tryEmitValue);
    }

}
