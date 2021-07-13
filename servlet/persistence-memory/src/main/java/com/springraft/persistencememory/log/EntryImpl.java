package com.springraft.persistencememory.log;

import com.springraft.persistence.log.Entry;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EntryImpl implements Entry {

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
    public EntryImpl(Long term, String command) {
        this.index = null;
        this.term = term;
        this.command = command;
    }

    /**
     * Specific constructor for cloning
     *
     * @param entry Entry to clone
     * */
    public EntryImpl(EntryImpl entry) {
        this.index = entry.getIndex();
        this.term = entry.getTerm();
        this.command = entry.getCommand();
    }

    /* --------------------------------------------------- */

    @Override
    public Entry Entry(long index, long term, String command) {
        return new EntryImpl(index, term, command);
    }

    /* --------------------------------------------------- */

    public EntryImpl clone() {
        return new EntryImpl(this);
    }

}
