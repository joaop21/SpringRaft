package com.springraft.persistencer2dbc.state;

import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class State extends com.springraft.persistence.state.StateModel implements Persistable<Long> {

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    @Override
    public boolean isNew() {
        return isNew;
    }

}
