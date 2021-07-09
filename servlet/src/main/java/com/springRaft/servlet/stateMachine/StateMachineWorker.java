package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.persistence.log.Entry;
import com.springRaft.servlet.persistence.log.LogService;
import com.springRaft.servlet.persistence.log.LogState;
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

    /* Map that contains the clients waiting requests */
    protected final WaitingRequests waitingRequests;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for state to changes */
    private final Condition newCommitCondition;

    /* Dictates whether there are new commitments */
    private boolean newCommits;

    /* --------------------------------------------------- */

    @Autowired
    public StateMachineWorker(StateMachineStrategy stateMachineStrategy, LogService logService, WaitingRequests waitingRequests) {
        this.strategy = stateMachineStrategy;
        this.logService = logService;
        this.waitingRequests = waitingRequests;
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
        long indexToStart = logState.getLastApplied() + 1;

        for (long index = indexToStart ; index <= logState.getCommittedIndex() ; index++) {

            // get command to apply
            Entry entry = this.logService.getEntryByIndex(index);
            if (entry == null) {
                // notify client of the response
                this.waitingRequests.putResponse(index, null);
                continue;
            }
            String command = entry.getCommand();

            // apply command depending on the strategy
            Object response = this.strategy.apply(command);

            // increment lastApplied in the Log State
            this.logService.incrementLastApplied();

            // notify client of the response
            this.waitingRequests.putResponse(index, response);

        }

    }

}
