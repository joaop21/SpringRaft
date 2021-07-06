package com.springraft.persistencer2dbc.state;

import com.springraft.persistence.state.State;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Data
@Table(value = "state")
public class StateImpl implements State, Persistable<Long> {

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
    public State State(long id, long currentTerm, String votedFor, boolean isNew) {
        return new StateImpl(id, currentTerm, votedFor, isNew);
    }

    /* --------------------------------------------------- */

    @Override
    public boolean isNew() {
        return isNew;
    }
}
