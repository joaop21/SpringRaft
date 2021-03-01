package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.communication.outbound.MessageSubscriber;
import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.Candidate;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")
public class PeerWorker implements Runnable, MessageSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Candidate.class);

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* String of the address of the target server */
    private final String targetServerName;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for state to changes */
    private final Condition newMessages;

    /* Remaining messages to send */
    private Integer remainingMessages;


    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            InetSocketAddress targetServer
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServer.getHostName() + ":" + targetServer.getPort();
        this.lock = new ReentrantLock();
        this.newMessages = lock.newCondition();
        this.remainingMessages = 0;
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {
        log.info("Start Peer Worker that handles communications with " + this.targetServerName);

        while (true) {

            this.waitForNewMessages();

            // get message to send
            Message message = this.consensusModule.getNextMessage(this.targetServerName);

            // send message
            this.send(message);

        }

    }

    /**
     * Method that waits for new messages.
     * */
    private void waitForNewMessages() {
        lock.lock();
        try {

            while(this.remainingMessages == 0)
                this.newMessages.await();

            this.remainingMessages--;

        } catch (InterruptedException e) {
            log.error("Break Await", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO
     * */
    private void send(Message message) {

        if(message instanceof RequestVote) {

            RequestVoteReply reply;
            do {
                reply = this.requestVote(message);
            } while (reply == null && this.remainingMessages == 0);

        }

        // elseif for append entries

    }

    /**
     * TODO
     * */
    private RequestVoteReply requestVote(Message message) {

        long start = System.currentTimeMillis();

        try {

            return this.outbound.requestVote(this.targetServerName, (RequestVote) message);

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            long remaining = this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - start);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException ignored) {}
            }

            return null;

        } catch (TimeoutException e) {
            // If the request vote communication exceeded heartbeat timout

            log.warn("Communication to server " + this.targetServerName + " exceeded heartbeat timeout!!");

            return null;

        } catch (Exception e) {
            // If another exception occurs

            log.error("EXCEPTION NOT EXPECTED", e);
            return null;

        }

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Override
    public void newMessage() {
        lock.lock();
        try {
            this.remainingMessages++;
            this.newMessages.signal();
        } finally {
            lock.unlock();
        }
    }
}
