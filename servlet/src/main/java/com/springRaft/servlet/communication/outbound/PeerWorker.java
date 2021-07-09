package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.*;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import com.springRaft.servlet.util.ConcurrentQueue;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope("prototype")
public class PeerWorker implements Runnable, MessageSubscriber {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(PeerWorker.class);

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Module that has the consensus functions to invoke */
    private final ConsensusModule consensusModule;

    /* Raft properties that need to be accessed */
    private final RaftProperties raftProperties;

    /* String of the address of the target server */
    private final String targetServerName;

    /* Queue for publish new rpcs */
    private final ConcurrentQueue<Callable<Void>> rpcQueue;

    /* Disposable of the ongoing communication */
    private Future<Void> ongoingCommunication;

    /* Flag that marks if the ongoing communication is an heartbeat */
    private final AtomicBoolean isHeartbeat;

    /* Timestamp in Milliseconds that marks the beginning of the last communication */
    private final AtomicLong communicationStart;

    /* Task Executor for submit workers to execution */
    private final ThreadPoolTaskExecutor taskExecutor;

    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            String targetServerName,
            ThreadPoolTaskExecutor taskExecutor
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServerName;

        this.rpcQueue = new ConcurrentQueue<>();

        this.ongoingCommunication = null;
        this.isHeartbeat = new AtomicBoolean(false);
        this.communicationStart = new AtomicLong(0);

        this.taskExecutor = taskExecutor;
    }

    /* --------------------------------------------------- */

    @Override
    public void sendRequestVote(RequestVote requestVote) {
        this.rpcQueue.add(() -> {
            this.handleRequestVote(requestVote);
            return null;
        });
    }

    @Override
    public void sendAuthorityHeartbeat(AppendEntries heartbeat) {
        this.rpcQueue.add(() -> {
            this.handleHeartbeat(heartbeat);
            return null;
        });
    }

    @Override
    public void sendHeartbeat(AppendEntries heartbeat, String to) {
        this.rpcQueue.add(() -> {
            this.handleHeartbeat(heartbeat);
            return null;
        });
    }

    @Override
    public void sendAppendEntries(AppendEntries appendEntries, String to) {
        this.rpcQueue.add(() -> {
            this.handleNormalAppendEntries(appendEntries);
            return null;
        });
    }

    @Override
    public void newFollowerState() {
        this.rpcQueue.add(() -> null);
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

        while (true) {

             Callable<Void> rpc = this.rpcQueue.poll();

            if (this.ongoingCommunication != null && !this.ongoingCommunication.isCancelled())
                 this.ongoingCommunication.cancel(true);

             this.ongoingCommunication = this.taskExecutor.submit(rpc);

        }
    }

    /* --------------------------------------------------- */

    @Override
    @Synchronized
    public void newClientRequest() {

        if (this.isHeartbeat.get()) {

            if (this.ongoingCommunication != null && !this.ongoingCommunication.isCancelled())
                this.ongoingCommunication.cancel(true);

            this.ongoingCommunication = null;
            this.isHeartbeat.set(false);

            this.taskExecutor.execute(() -> this.consensusModule.appendEntriesReply(null, this.targetServerName));

        }

    }

    /* --------------------------------------------------- */

    /**
     * Method that handles the requestVoteRPC communication.
     *
     * @param requestVote Message to send to the target server.
     * */
    private void handleRequestVote(RequestVote requestVote) {

        RequestVoteReply reply;

        do {

            try {
                reply = (RequestVoteReply) this.sendRPCHandler(() -> this.outbound.requestVote(this.targetServerName, requestVote));
            } catch (InterruptedException e) {
                return;
            }

        } while (reply == null);

        RequestVoteReply finalReply = reply;
        this.taskExecutor.execute(() -> this.consensusModule.requestVoteReply(finalReply));

    }

    /**
     * Method that handles the heartbeats communication.
     *
     * @param appendEntries Message to send to the target server.
     * */
    private void handleHeartbeat(AppendEntries appendEntries) {

        AppendEntriesReply reply;

        this.isHeartbeat.set(true);

        do {

            try {
                long duration = this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - this.communicationStart.get());
                if (duration > 0)
                    Thread.sleep(duration);
            } catch (InterruptedException e) {
                return;
            }

            this.communicationStart.set(System.currentTimeMillis());

            try {
                reply = (AppendEntriesReply) this.sendRPCHandler(() -> this.outbound.appendEntries(this.targetServerName, appendEntries));
            } catch (InterruptedException e) {
                return;
            }

        } while (reply == null);

        AppendEntriesReply finalReply = reply;
        this.taskExecutor.execute(() -> this.consensusModule.appendEntriesReply(finalReply, this.targetServerName));

    }

    /**
     * Method that handles a normal appendEntries communication.
     *
     * @param appendEntries Message to send to the target server.
     * */
    private void handleNormalAppendEntries(AppendEntries appendEntries) {

        AppendEntriesReply reply;

        do {

            this.communicationStart.set(System.currentTimeMillis());

            try {
                reply = (AppendEntriesReply) this.sendRPCHandler(() -> this.outbound.appendEntries(this.targetServerName, appendEntries));
            } catch (InterruptedException e) {
                return;
            }

        } while (reply == null);

        AppendEntriesReply finalReply = reply;
        this.taskExecutor.execute(() -> this.consensusModule.appendEntriesReply(finalReply, this.targetServerName));

    }

    /**
     * Method that handles the communication of an RPC:
     *      1- Sends the RPC and waits for the response;
     *      2- If:
     *          a) The response is fine, just retrieve its value;
     *          b) If the target server is not alive, it waits at most the heartbeat time to retrieve no response;
     *          c) If the server didn't respond within the heartbeat time, no response is retrieved;
     *
     * @param rpc The Callable function that represents the RPC to invoke.
     *
     * @return Response Message.
     * */
    private Message sendRPCHandler(Callable<? extends Message> rpc) throws InterruptedException {

        long start = System.currentTimeMillis();

        try {

            return rpc.call();

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            Thread.sleep(this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - start));

        } catch (TimeoutException e) {

            // If the communication exceeded heartbeat timeout
            log.warn("Communication to server " + this.targetServerName + " exceeded heartbeat timeout!!");

        } catch (Exception e) {

            // If another exception occurs
            log.error("Exception not expected in sendRPCHandler method");

        }

        return null;

    }

}
