package com.springRaft.reactive.config.startup;

import com.springRaft.reactive.persistence.state.State;
import com.springRaft.reactive.persistence.state.StateService;
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

    /* --------------------------------------------------- */

    @Override
    public void run(ApplicationArguments args) {

        this.stateService.getState()
                .switchIfEmpty(
                        Mono.defer(() ->
                                Mono.just(this.applicationContext.getBean("InitialState", State.class))
                        )
                )
                .doOnNext(state -> this.stateService.saveState(state).subscribe())
                .subscribe();

    }

}
