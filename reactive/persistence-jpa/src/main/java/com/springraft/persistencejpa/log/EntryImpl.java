package com.springraft.persistencejpa.log;

import com.springraft.persistence.log.Entry;
import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "entry")
public class EntryImpl implements Entry {

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

}
