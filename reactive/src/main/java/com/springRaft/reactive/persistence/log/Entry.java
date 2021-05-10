package com.springRaft.reactive.persistence.log;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Entry implements Persistable<Long> {

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

    /**
     * Specific constructor for creating a parametrized entry.
     * */
    public Entry(Long term, String command, boolean isNew) {
        this.index = null;
        this.term = term;
        this.command = command;
        this.isNew = isNew;
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