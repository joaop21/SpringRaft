package com.springraft.persistencer2dbc.log;

import com.springraft.persistence.log.Entry;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Data
@Table(value = "entry")
public class EntryImpl implements Entry, Persistable<Long> {

    /* Index of an Entry in the log */
    @Id
    private Long index;

    /* Term in which this entry was inserted */
    private Long term;

    /* Command to apply to the state machine */
    private String command;

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    public EntryImpl(long term, String command, boolean isNew) {
        this.index = null;
        this.term = term;
        this.command = command;
        this.isNew = isNew;
    }

    /* --------------------------------------------------- */

    @Override
    public Entry Entry(long index, long term, String command, boolean isNew) {
        return new EntryImpl(index, term, command, isNew);
    }

    /* --------------------------------------------------- */

    @Override
    public Long getId() {
        return this.index;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
