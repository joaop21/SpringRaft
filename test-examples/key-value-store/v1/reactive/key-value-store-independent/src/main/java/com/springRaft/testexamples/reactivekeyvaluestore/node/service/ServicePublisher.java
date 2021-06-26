package com.springRaft.testexamples.reactivekeyvaluestore.node.service;

import com.springRaft.testexamples.reactivekeyvaluestore.node.Node;
import com.springRaft.testexamples.reactivekeyvaluestore.node.NodeRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@Scope("singleton")
@ConditionalOnProperty(name = "node.service.strategy", havingValue = "Publisher")
public class ServicePublisher implements NodeService {

    private final Sinks.Many<Mono<Node>> sink;

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    public ServicePublisher(NodeRepository repository) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.repository = repository;
        this.servicePublisher().subscribe();
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<Node> get(String key) {

        // JUST
        /*
        Sinks.One<Node> responseSink = Sinks.one();

        return Mono.just(responseSink)
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeGet(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);*/

        // DEFER
        /*return Mono.defer(() -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            return Mono.just(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeGet(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);*/

        // CREATE
        return Mono.<Sinks.One<Node>>create(oneMonoSink -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            this.sink.tryEmitNext(this.safeGet(key, sinkNode));
            oneMonoSink.success(sinkNode);
        })
                .flatMap(Sinks.Empty::asMono);

    }

    @Override
    public Mono<List<Node>> upsert(String key, String value) {

        // JUST
        /*
        Sinks.Many<Node> responseSink = Sinks.many().unicast().onBackpressureBuffer();

        return Mono.just(responseSink)
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeUpsert(key, value, sinkNode)))
                .flatMapMany(Sinks.Many::asFlux)
                .collectList();*/

        // DEFER
        /*
        return Mono.<Sinks.Many<Node>>create(createSink -> {
            Sinks.Many<Node> sinkNode = Sinks.many().unicast().onBackpressureBuffer();
            createSink.success(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeUpsert(key, value, sinkNode)))
                .flatMapMany(Sinks.Many::asFlux)
                .collectList();*/

        // CREATE
        return Mono.<Sinks.Many<Node>>create(manyMonoSink -> {
            Sinks.Many<Node> sinkNode = Sinks.many().unicast().onBackpressureBuffer();
            this.sink.tryEmitNext(this.safeUpsert(key, value, sinkNode));
            manyMonoSink.success(sinkNode);
        })
                .flatMapMany(Sinks.Many::asFlux)
                .collectList();

    }

    @Override
    public Mono<Node> delete(String key) {

        // JUST
        /*
        Sinks.One<Node> responseSink = Sinks.one();

        return Mono.just(responseSink)
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeDelete(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);*/

        // DEFER
        /*
        return Mono.defer(() -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            return Mono.just(sinkNode);
        })
                .doOnNext(sinkNode -> this.sink.tryEmitNext(this.safeDelete(key, sinkNode)))
                .flatMap(Sinks.Empty::asMono);*/

        // CREATE
        return Mono.<Sinks.One<Node>>create(nodeMonoSink -> {
            Sinks.One<Node> sinkNode = Sinks.one();
            this.sink.tryEmitNext(this.safeDelete(key, sinkNode));
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
