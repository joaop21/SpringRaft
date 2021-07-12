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

    /* --------------------------------------------------- */

    @Override
    public State State(long id, long currentTerm, String votedFor) {
        return new StateImpl(id, currentTerm, votedFor);
    }

}
