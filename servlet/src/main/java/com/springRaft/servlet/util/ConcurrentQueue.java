package com.springRaft.servlet.util;

import java.util.LinkedList;
import java.util.Queue;

public class ConcurrentQueue<T> {
    private final Queue<T> queue = new LinkedList<>();

    /**
     * Blocking method that consumes the head of the queue when its available.
     *
     * @return Object Object that is inside of the queue.
     * */
    public synchronized T poll(){
        while(this.queue.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.queue.poll();
    }

    /**
     *  Method that adds an object to the queue.
     *
     * @param obj Object to be inserted in the queue.
     * */
    public synchronized void add(T obj){
        this.queue.add(obj);
        notify();
    }

    /**
     * Method for getting the current size of the queue.
     *
     * @return int that reflects the queue size.
     * */
    public synchronized int size(){
        return this.queue.size();
    }

}
