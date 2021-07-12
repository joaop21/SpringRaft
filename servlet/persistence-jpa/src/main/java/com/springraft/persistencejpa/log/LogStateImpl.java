package com.springraft.persistencejpa.log;

import com.springraft.persistence.log.LogState;
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
@Table(name = "log_state")
public class LogStateImpl implements LogState {

    /* Id of the object */
    @Id
    private Long id;

    /* Index of the highest log entry known to be committed */
    private Long committedIndex;

    /* Term of the highest log entry known to be committed */
    private Long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private Long lastApplied;

    /* --------------------------------------------------- */

    @Override
    public LogState LogState(Long id, Long committedIndex, Long committedTerm, Long lastApplied) {
        return new LogStateImpl(id, committedIndex, committedTerm, lastApplied);
    }

}
