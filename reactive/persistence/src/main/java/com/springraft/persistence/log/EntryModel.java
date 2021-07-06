package com.springraft.persistence.log;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public abstract class EntryModel {

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
    public EntryModel(Long term, String command) {
        this.index = null;
        this.term = term;
        this.command = command;
    }

}
