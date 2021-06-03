package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@Scope("singleton")
@AllArgsConstructor
public class ServicePublisher {

    private final Sinks.Many<Mono<Node>> sink = Sinks.many().multicast().onBackpressureBuffer();

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    public Mono<Node> get(String key) {

        return Mono.defer(() -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            return Mono.just(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeGet(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);

    }

    public Mono<List<Node>> upsert(String key, String value) {

        return Mono.<Sinks.Many<Node>>create(createSink -> {
            Sinks.Many<Node> sinkNode = Sinks.many().unicast().onBackpressureBuffer();
            createSink.success(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeUpsert(key, value, sinkNode)))
                .flatMapMany(Sinks.Many::asFlux)
                .collectList();

    }

    public Mono<Node> delete(String key) {

        return Mono.defer(() -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            return Mono.just(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeDelete(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);

    }

    /* --------------------------------------------------- */

    public Flux<?> servicePublisher() {
        return this.sink.asFlux().flatMap(mono -> mono, 1);
    }

    /* --------------------------------------------------- */

    private Mono<Node> safeGet(String key, Sinks.One<Node> sinkOne) {
        return this.repository.findNodeByKey(key)
                .doOnSuccess(sinkOne::tryEmitValue);
    }

    private Mono<Node> safeUpsert(String key, String text, Sinks.Many<Node> sinkMany) {
        return this.repository.findNodeByKey(key)
                .flatMap(node ->
                        repository.deleteNodeByKey(key)
                                .doOnSuccess(result -> sinkMany.tryEmitNext(node))
                )
                .then(this.repository.save(new Node(key,text.replaceFirst("value=", ""))))
                .doOnNext(savedNode -> {
                    sinkMany.tryEmitNext(savedNode);
                    sinkMany.tryEmitComplete();
                });
    }

    private Mono<Node> safeDelete(String key, Sinks.One<Node> sinkNode) {
        return this.repository.findNodeByKey(key)
                .flatMap(node ->
                        this.repository.deleteNodeByKey(key)
                                .then(Mono.just(node))
                )
                .doOnSuccess(sinkNode::tryEmitValue);
    }

}
