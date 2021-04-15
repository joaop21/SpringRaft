package com.springRaft.reactive.consensusModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Follower implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Follower.class);

    /* --------------------------------------------------- */

    @Override
    public void start() {

        log.info("\n\nFOLLOWER\n");

    }

}
