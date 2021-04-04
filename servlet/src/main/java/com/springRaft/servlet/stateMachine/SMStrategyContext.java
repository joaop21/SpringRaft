package com.springRaft.servlet.stateMachine;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.worker.StateMachineWorker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

@AllArgsConstructor
public class SMStrategyContext {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(StateMachineWorker.class);

    /* Raft properties that need to be accessed */
    protected final RaftProperties raftProperties;

    /* Outbound context for communication to other servers */
    protected final OutboundContext outbound;

    /* --------------------------------------------------- */

    public Object apply(String command) {

        Object reply = null;

        do {

            long start = System.currentTimeMillis();

            try {

                reply = this.outbound.request(command, this.raftProperties.getApplicationServer());

            } catch (ExecutionException e) {
                // If target server is not alive

                log.warn("Application Server is not up!!");

                // sleep for the remaining time, if any
                // based on heartbeat
                this.sleepForSomeTime(start);

            } catch (Exception e) {

                // If another exception occurs
                log.error("Exception not expected in IndependentServer apply method");

            }

        } while (reply == null);

        return reply;
    }

    /* --------------------------------------------------- */

    /**
     * Method that makes a thread sleep for an amount of time.
     *
     * @param startTime Time in milliseconds used to calculate the remaining time
     *                  until the thread has to continue executing.
     * */
    private void sleepForSomeTime(long startTime) {

        long remaining = this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - startTime);

        if (remaining > 0) {

            try {

                Thread.sleep(remaining);

            } catch (InterruptedException exception) {

                log.error("Exception while sleeping in sleepForSomeTime method");

            }

        }

    }

}
