package com.springRaft.reactive.persistence.log;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LogState implements Persistable<Long> {

    /* Id of the object */
    @Id
    private Long id;

    /* Index of the highest log entry known to be committed */
    private Long committedIndex;

    /* Term of the highest log entry known to be committed */
    private Long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private Long lastApplied;

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    @Override
    public boolean isNew() {
        return isNew;
    }
}
