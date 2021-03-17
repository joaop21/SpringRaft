package com.springRaft.servlet.worker;

import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.stateMachine.CommitmentSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("singleton")
public class StateMachineWorker implements Runnable, CommitmentSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Service to access persisted log repository */
    private final LogService logService;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for state to changes */
    private final Condition newCommitCondition;

    /* Dictates whether there are new commitments */
    private boolean newCommits;

    /* Log's current and updated state for lookup optimization */
    private LogState logState;

    /* --------------------------------------------------- */

    @Autowired
    public StateMachineWorker(LogService logService) {
        this.logService = logService;
        this.lock = new ReentrantLock();
        this.newCommitCondition = this.lock.newCondition();
        this.newCommits = false;
        this.logState = this.logService.getState();
    }

    /* --------------------------------------------------- */

    @Override
    public void newCommit() {
        lock.lock();
        this.newCommits = true;
        this.newCommitCondition.signal();
        lock.unlock();
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

        log.info("\n\nState Machine Worker working...\n\n");

        // start working



    }

}
