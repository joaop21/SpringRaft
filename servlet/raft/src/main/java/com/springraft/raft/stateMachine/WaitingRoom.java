package com.springraft.raft.stateMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")
public class WaitingRoom {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(WaitingRoom.class);

    /* Mutex for some operations */
    private final Lock l = new ReentrantLock();

    /* Condition where a thread can wait for state to changes */
    private final Condition cond = l.newCondition();

    /* Response to deliver to client */
    private Object response = null;

    /* Boolean that dictates whether this waiting room has a response */
    private boolean hasResponse = false;

    /* --------------------------------------------------- */

    /**
     * Method that blocks a thread, and waits for a response.
     *
     * @return Object that is the generic way to respond something.
     * */
    public Object getResponse(){

        l.lock();
        try{

            while(response == null && !hasResponse)
                cond.await();

        } catch (InterruptedException e) {

            log.error("Exception while awaiting on getResponse");

        } finally {
            l.unlock();
        }

        return response;

    }

    /**
     * Method for putting a null response.
     * */
    void putResponse() {

        this.putResponse(null);

    }

    /**
     * Method for put a response in this waiting room and notify the thread waiting for this response.
     *
     * @param response Object to respond.
     * */
    void putResponse(Object response){

        l.lock();
        try {

            this.response = response;
            this.hasResponse = true;
            cond.signal();

        } finally {
            l.unlock();
        }

    }

}

