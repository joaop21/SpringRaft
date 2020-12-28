package com.springraft.testexamples.servletstack.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Counter {

    /*--------------------------------------------------------------------------------*/

    /* Id of the object */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /* Value of the counter */
    private long value;

    /* Version of the counter (Optimistic Locking) */
    @Version
    private long version;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public void setValue(long value) {
        this.value = value;
    }
}
