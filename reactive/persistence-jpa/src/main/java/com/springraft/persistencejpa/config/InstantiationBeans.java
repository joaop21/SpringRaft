package com.springraft.persistencejpa.config;

import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.State;
import com.springraft.persistencejpa.log.LogStateImpl;
import com.springraft.persistencejpa.state.StateImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstantiationBeans {

    /**
     * Bean for creating LogState Objects.
     *
     * @return LogState created with initialization arguments.
     * */
    @Bean(name = "InitialLogState")
    public LogState newLogState() {
        return new LogStateImpl((long) 1, (long) 0, (long) 0, (long) 0, true);
    }

    /**
     * Bean for creating State Objects.
     *
     * @return State created with initialization arguments.
     * */
    @Bean(name = "InitialState")
    public State newState() {
        return new StateImpl((long) 1,(long) 1,null, true);
    }

}
