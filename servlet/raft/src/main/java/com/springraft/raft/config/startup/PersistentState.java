package com.springraft.raft.config.startup;

import com.springraft.persistence.log.LogService;
import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.State;
import com.springraft.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@AllArgsConstructor
public class PersistentState implements ApplicationRunner {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* Service to access persisted log repository */
    private final LogService logService;

    /* --------------------------------------------------- */

    /**
     * Check persisted state.
     * */
    @Override
    public void run(ApplicationArguments args) {

        this.CheckAndSetState();

        this.CheckAndSetLogState();

    }

    /**
     * Startup action to check if a state exists in a DB.
     * If not, create one and persist it.
     * */
    private void CheckAndSetState() {

        State state = this.stateService.getState();

        if (state == null) {
            State new_state = applicationContext.getBean("InitialState", State.class);
            state = stateService.saveState(new_state);

            assert state.getId() == 1 && state.getCurrentTerm() == 1;
        }

    }

    /**
     * Startup action to check if a log state exists in a DB.
     * If not, create one and persist it.
     * */
    private void CheckAndSetLogState() {

        LogState logState = this.logService.getState();

        if (logState == null) {
            LogState new_state = applicationContext.getBean("InitialLogState", LogState.class);

            logState = logService.saveState(new_state);

            assert logState.getId() == 1 &&
                    logState.getCommittedIndex() == 0 &&
                    logState.getCommittedTerm() == 0 &&
                    logState.getLastApplied() == 0;
        }

    }

}

