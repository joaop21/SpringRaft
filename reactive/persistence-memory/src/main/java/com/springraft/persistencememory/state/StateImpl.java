package com.springraft.persistencememory.state;

import com.springraft.persistence.state.State;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StateImpl implements State {

    /* Id of the object */
    private Long id;

    /* Value for the current term */
    private Long currentTerm;

    /* Who the vote was for in the current term */
    private String votedFor;

    /* Transient flag that states ith the object is new or already in database */
    private boolean isNew;

    /* --------------------------------------------------- */

    public StateImpl(StateImpl state) {
        this.id = state.getId();
        this.currentTerm = state.getCurrentTerm();
        this.votedFor = state.getVotedFor();
        this.isNew = state.isNew();
    }

    /* --------------------------------------------------- */

    @Override
    public State State(long id, long currentTerm, String votedFor, boolean isNew) {
        return new StateImpl(id, currentTerm, votedFor, isNew);
    }

    /* --------------------------------------------------- */

    public StateImpl clone() {
        return new StateImpl(this);
    }

}