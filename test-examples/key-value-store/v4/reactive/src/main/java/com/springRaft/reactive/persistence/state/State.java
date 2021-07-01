package com.springRaft.reactive.persistence.state;

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
public class State implements Persistable<Long> {

    /* Id of the object */
    @Id
    private Long id;

    /* Value for the current term */
    private Long currentTerm;

    /* Who the vote was for in the current term */
    private String votedFor;

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    @Override
    public boolean isNew() {
        return isNew;
    }
}
