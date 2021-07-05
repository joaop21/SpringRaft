package com.springraft.persistence.log;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Entry {

    /* Index of an Entry in the log */
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
