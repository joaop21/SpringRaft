package com.springraft.testexamples.servletstack.models;

public class Counter {

    private long value = 0;

    public synchronized Long increment() {
        return ++this.value;
    }

    public synchronized Long decrement() {
        return --this.value;
    }

    public Long get() {
        return this.value;
    }

}
