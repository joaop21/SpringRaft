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

    /* Transient flag that states ith the object is new or already in database */
    private boolean isNew;

    /* --------------------------------------------------- */

    /**
     * Specific constructor for ge
     * */
    public EntryImpl(Long term, String command, boolean isNew) {
        this.index = null;
        this.term = term;
        this.command = command;
        this.isNew = isNew;
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
        this.isNew = entry.isNew();
    }

    /* --------------------------------------------------- */

    @Override
    public Entry Entry(long index, long term, String command, boolean isNew) {
        return new EntryImpl(index, term, command, isNew);
    }

    /* --------------------------------------------------- */

    public EntryImpl clone() {
        return new EntryImpl(this);
    }

}
