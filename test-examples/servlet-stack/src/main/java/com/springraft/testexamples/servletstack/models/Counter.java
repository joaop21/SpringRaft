package com.springraft.testexamples.servletstack.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Counter {

    /*--------------------------------------------------------------------------------*/

    /* Id of the object */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long value;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    protected Counter() {
        this.value = 0;
    }

    /**
     * TODO
     * */
    public Counter(long id, long value) {
        this.id = id;
        this.value = value;
    }

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public long getId() {
        return id;
    }

    /**
     * TODO
     * */
    public long getValue() {
        return value;
    }

    /**
     * TODO
     * */
    public void setValue(long value) {
        this.value = value;
    }
}
