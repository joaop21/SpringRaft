package com.springRaft.servlet.worker;

import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
import com.springRaft.servlet.stateMachine.CommitmentSubscriber;
import com.springRaft.servlet.stateMachine.StateMachineStrategy;
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

    /* Strategy for applying commands to the State Machine */
    private StateMachineStrategy strategy;

    /* Service to access persisted log repository */
    private final LogService logService;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for state to changes */
    private final Condition newCommitCondition;

    /* Dictates whether there are new commitments */
    private boolean newCommits;

    /* --------------------------------------------------- */

    @Autowired
    public StateMachineWorker(LogService logService) {
        this.strategy = null;
        this.logService = logService;
        this.lock = new ReentrantLock();
        this.newCommitCondition = this.lock.newCondition();
        this.newCommits = false;
    }

    /* --------------------------------------------------- */

    public void setStrategy(StateMachineStrategy stateMachineStrategy) {
        this.strategy = stateMachineStrategy;
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


        while (true) {

            this.waitForNewCommits();

            this.applyCommitsToStateMachine();

        }

    }

    /**
     * Method that waits for new commits.
     * */
    private void waitForNewCommits() {

        lock.lock();
        try {

            while (!this.newCommits)
                this.newCommitCondition.await();

            this.newCommits = false;

        } catch (InterruptedException interruptedException) {

            log.error("Exception while awaiting on newCommitCondition");

        } finally {
            lock.unlock();
        }

    }

    /**
     * TODO
     * */
    private void applyCommitsToStateMachine() {

        LogState logState = this.logService.getState();
        long entriesToCommit = logState.getCommittedIndex() - logState.getLastApplied();

        for (long index = logState.getCommittedIndex() ; index < entriesToCommit ; index++) {

            // get command to apply
            String command = this.logService.getEntryByIndex(index).getCommand();

            // apply command depending on the strategy
            this.strategy.apply(command);

            // increment lastApplied in the Log State
            this.logService.incrementLastApplied();

        }

    }

}
