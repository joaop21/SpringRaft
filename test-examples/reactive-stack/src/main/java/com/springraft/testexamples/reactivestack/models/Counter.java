package com.springraft.testexamples.reactivestack.models;

import reactor.core.publisher.Mono;

public class Counter {

    /*--------------------------------------------------------------------------------*/

    private long value = 0;

    /*--------------------------------------------------------------------------------*/

    public synchronized Mono<Long> increment() {
        return Mono.just(++this.value);
    }

    public synchronized Mono<Long> decrement() {
        return Mono.just(--this.value);
    }

    public Mono<Long> get() {
        return Mono.just(value);
    }

}
