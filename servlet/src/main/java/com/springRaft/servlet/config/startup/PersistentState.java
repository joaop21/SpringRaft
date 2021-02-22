package com.springRaft.servlet.config.startup;

import com.springRaft.servlet.persistence.state.State;
import com.springRaft.servlet.persistence.state.StateService;
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

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* --------------------------------------------------- */

    /**
     * Startup action to check if a state exists in a DB.
     * If not create one and persist it.
     * */
    @Override
    public void run(ApplicationArguments args) {

        State state = this.stateService.getState();

        if (state == null) {
            State new_state = applicationContext.getBean("InitialState", State.class);
            state = stateService.saveState(new_state);

            assert state.getId() == 1 && state.getCurrentTerm() == 1;
        }

    }

}
