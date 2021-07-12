package com.springraft.persistencejpa.state;

import com.springraft.persistence.state.State;
import lombok.*;
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
@Table(name = "state")
public class StateImpl implements State {

    /* Id of the object */
    @Id
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
