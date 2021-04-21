package com.springRaft.reactive.config;

import com.springRaft.reactive.persistence.state.State;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConstructorBeans {

    /**
     * Bean for creating State Objects.
     *
     * @return State created with initialization arguments.
     * */
    @Bean(name = "InitialState")
    public State newState() {
        return new State((long) 1,(long) 1,null, true);
    }

}
