package com.springraft.persistencememory.log;

import com.springraft.persistence.log.LogState;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LogStateImpl implements LogState {

    /* Id of the object */
    private Long id;

    /* Index of the highest log entry known to be committed */
    private Long committedIndex;

    /* Term of the highest log entry known to be committed */
    private Long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private Long lastApplied;

    /* --------------------------------------------------- */

    public LogStateImpl(LogStateImpl logState) {
        this.id = logState.getId();
        this.committedIndex = logState.getCommittedIndex();
        this.committedTerm = logState.getCommittedTerm();
        this.lastApplied = logState.getLastApplied();
    }

    /* --------------------------------------------------- */

    @Override
    public LogState LogState(Long id, Long committedIndex, Long committedTerm, Long lastApplied) {
        return new LogStateImpl(id, committedIndex, committedTerm, lastApplied);
    }

    /* --------------------------------------------------- */

    public LogStateImpl clone() {
        return new LogStateImpl(this);
    }

}
