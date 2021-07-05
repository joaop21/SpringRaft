package com.springraft.persistencer2dbc.log;

import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Entry extends com.springraft.persistence.log.EntryModel implements Persistable<Long> {

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    public Entry(long index, long term, String command, boolean isNew) {
        super(index,term,command);
        this.isNew = isNew;
    }

    /* --------------------------------------------------- */

    @Override
    public Long getId() {
        return this.getIndex();
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
