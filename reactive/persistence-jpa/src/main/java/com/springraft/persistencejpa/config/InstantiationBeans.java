package com.springraft.persistencejpa.config;

import com.springraft.persistence.log.Entry;
import com.springraft.persistence.log.LogState;
import com.springraft.persistence.state.State;
import com.springraft.persistencejpa.log.EntryImpl;
import com.springraft.persistencejpa.log.LogStateImpl;
import com.springraft.persistencejpa.state.StateImpl;
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
        return new LogStateImpl((long) 1, (long) 0, (long) 0, (long) 0, true);
    }

    /* --------------------------------------------------- */

    /**
     * Bean for creating State Objects.
     *
     * @return State created with initialization arguments.
     * */
    @Bean(name = "InitialState")
    public State newState() {
        return new StateImpl((long) 1,(long) 1,null, true);
    }

    /**
     * TODO
     * */
    @Bean(name = "NullState")
    public State nullState() {
        return new StateImpl(null, null, null, true);
    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Bean(name = "ParametrisedEntry")
    @Scope(value = "prototype")
    public Entry newParametrisedEntry(long term, String command, boolean isNew) {
        return new EntryImpl(term, command, isNew);
    }

    /**
     * TODO
     * */
    @Bean(name = "NullEntry")
    public Entry nullEntry() {
        return new EntryImpl(null, null, null, false);
    }

    /**
     * TODO
     * */
    @Bean(name = "EntryZero")
    public Entry entryZero() {
        return new EntryImpl((long) 0, (long) 0, null, false);
    }

}
