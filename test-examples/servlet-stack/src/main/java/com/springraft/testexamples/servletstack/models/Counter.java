package com.springraft.testexamples.servletstack.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public void setValue(long value) {
        this.value = value;
    }
}
