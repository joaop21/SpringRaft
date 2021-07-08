package com.springRaft.testexamples.keyvaluestoreindependent.node.service;

import com.springRaft.testexamples.keyvaluestoreindependent.node.Node;
import com.springRaft.testexamples.keyvaluestoreindependent.node.NodeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.Optional;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Publisher")
public class ServicePublisher implements NodeService {

    private final NodeRepository repository;

    private final Scheduler jdbcScheduler;

    private final Sinks.Many<Mono<Optional<Node>>> sink;

    /* --------------------------------------------------- */

    public ServicePublisher(NodeRepository repository, @Qualifier("jdbcScheduler") Scheduler jdbcScheduler) {
        this.repository = repository;
        this.jdbcScheduler = jdbcScheduler;
        this.sink = Sinks.many().unicast().onBackpressureBuffer();

        this.servicePublisher().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Optional<Node>> get(String key) {

        return Mono.<Sinks.One<Optional<Node>>>create(oneMonoSink -> {
            Sinks.One<Optional<Node>> sinkNode = Sinks.one();
            while(this.sink.tryEmitNext(this.safeGet(key, sinkNode)) != Sinks.EmitResult.OK);
            oneMonoSink.success(sinkNode);
        })
                .flatMap(Sinks.Empty::asMono);

    }

    @Override
    public Mono<List<Node>> upsert(String key, String value) {

        return Mono.<Sinks.Many<Optional<Node>>>create(manyMonoSink -> {
            Sinks.Many<Optional<Node>> sinkNode = Sinks.many().unicast().onBackpressureBuffer();
            while(this.sink.tryEmitNext(this.safeUpsert(key, value, sinkNode)) != Sinks.EmitResult.OK);
            manyMonoSink.success(sinkNode);
        })
                .flatMapMany(Sinks.Many::asFlux)
                .map(Optional::get)
                .collectList();

    }

    @Override
    public Mono<Optional<Node>> delete(String key) {

        return Mono.<Sinks.One<Optional<Node>>>create(nodeMonoSink -> {
            Sinks.One<Optional<Node>> sinkNode = Sinks.one();
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

    private Mono<Optional<Node>> safeGet(String key, Sinks.One<Optional<Node>> sinkOne) {
        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler)
                .doOnSuccess(sinkOne::tryEmitValue);
    }

    private Mono<Optional<Node>> safeUpsert(String key, String text, Sinks.Many<Optional<Node>> sinkMany) {
        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler)
                .filter(Optional::isPresent)
                    .flatMap(node ->
                            Mono.fromCallable(() -> repository.deleteNodeByKey(key))
                                    .subscribeOn(jdbcScheduler)
                                    .doOnSuccess(result -> sinkMany.tryEmitNext(node))
                    )
                .then(
                        Mono.fromCallable(() -> this.repository.save(new Node(key,text.replaceFirst("value=", ""))))
                                .subscribeOn(jdbcScheduler)
                                .map(Optional::ofNullable)
                )
                .doOnNext(savedNode -> {
                    sinkMany.tryEmitNext(savedNode);
                    sinkMany.tryEmitComplete();
                });
    }

    private Mono<Optional<Node>> safeDelete(String key, Sinks.One<Optional<Node>> sinkNode) {
        return Mono.fromCallable(() -> this.repository.findNodeByKey(key))
                .subscribeOn(jdbcScheduler)
                .flatMap(node ->
                        Mono.fromCallable(() -> this.repository.deleteNodeByKey(key))
                                .subscribeOn(jdbcScheduler)
                                .then(Mono.just(node))
                )
                .doOnSuccess(sinkNode::tryEmitValue);
    }

}
