package com.springRaft.servlet.persistence.log;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Entry {

    /* Index of an Entry in the log */
    @Id
    private Long index;

    /* Term in which this entry was inserted */
    private Long term;

    /* Command to apply to the state machine */
    private String command;

    /* --------------------------------------------------- */

    /**
     * Specific constructor for ge
     * */
    public Entry(Long term, String command) {
        this.index = null;
        this.term = term;
        this.command = command;
    }

}
