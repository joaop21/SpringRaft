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
import reactor.core.publisher.Mono;

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

    @Override
    public void run(ApplicationArguments args) {

        this.checkAndSetState()
                .flatMap(state -> this.checkAndSetLogState())
                .block();

    }

    /**
     * Startup action to check if a state exists in a DB.
     * If not, create one and persist it.
     *
     * @return Mono<State> Persisted state.
     * */
    private Mono<State> checkAndSetState() {
        return this.stateService.getState()
                .switchIfEmpty(Mono.just((State)this.applicationContext.getBean("InitialState")))
                    .flatMap(this.stateService::saveState);
    }

    /**
     * Startup action to check if a log state exists in a DB.
     * If not, create one and persist it.
     *
     * @return Mono<LogState> Persisted log state.
     * */
    private Mono<LogState> checkAndSetLogState() {
        return this.logService.getState()
                .switchIfEmpty(Mono.just((LogState) this.applicationContext.getBean("InitialLogState")))
                .flatMap(this.logService::saveState)
                .cast(LogState.class)
                .doOnNext(logState -> System.out.println(logState.toString()));
    }

}
