package com.springraft.persistencejpa.log;

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
public class Entry extends com.springraft.persistence.log.Entry {

    /* --------------------------------------------------- */

    public Entry(long index, long term, String command) {
        super(index,term,command);
    }

    /* --------------------------------------------------- */

    @Override
    @Id
    public Long getIndex() {
        return super.getIndex();
    }

}
