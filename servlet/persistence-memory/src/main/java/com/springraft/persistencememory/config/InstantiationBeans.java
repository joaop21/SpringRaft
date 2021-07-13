package com.springraft.persistencememory.config;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.State;
import com.springraft.persistencememory.log.EntryImpl;
import com.springraft.persistencememory.log.LogStateImpl;
import com.springraft.persistencememory.state.StateImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class InstantiationBeans {

    /**
     * Bean for creating LogState Objects.
     *
     * @return LogState created with initialization arguments.
     * */
    @Bean(name = "InitialLogState")
    public LogState newLogState() {
        return new LogStateImpl((long) 1, (long) 0, (long) 0, (long) 0);
    }

    /* --------------------------------------------------- */

    /**
     * Bean for creating State Objects.
     *
     * @return State created with initialization arguments.
     * */
    @Bean(name = "InitialState")
    public State newState() {
        return new StateImpl((long) 1,(long) 1,null);
    }

    /**
     * TODO
     * */
    @Bean(name = "NullState")
    public State nullState() {
        return new StateImpl(null, null, null);
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Bean(name = "ParametrisedEntry")
    @Scope(value = "prototype")
    public Entry newParametrisedEntry(long term, String command) {
        return new EntryImpl(term, command);
    }

    /**
     * TODO
     * */
    @Bean(name = "NullEntry")
    public Entry nullEntry() {
        return new EntryImpl(null, null, null);
    }

    /**
     * TODO
     * */
    @Bean(name = "EntryZero")
    public Entry entryZero() {
        return new EntryImpl((long) 0, (long) 0, null);
    }

}
