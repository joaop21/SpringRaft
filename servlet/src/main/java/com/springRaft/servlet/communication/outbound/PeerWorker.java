package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.config.RaftProperties;
import com.springRaft.servlet.consensusModule.ConsensusModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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


    /* Flag that marks if the ongoing communication is an heartbeat */
    private final AtomicBoolean isHeartbeat;

    /* Timestamp in Milliseconds that marks the beginning of the last communication */
    private final AtomicLong communicationStart;

    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            ConsensusModule consensusModule,
            RaftProperties raftProperties,
            String targetServerName
    ) {
        this.outbound = outbound;
        this.consensusModule = consensusModule;
        this.raftProperties = raftProperties;
        this.targetServerName = targetServerName;

        this.isHeartbeat = new AtomicBoolean(false);
        this.communicationStart = new AtomicLong(0);
    }

    /* --------------------------------------------------- */

    @Override
    public void sendRequestVote(RequestVote requestVote) {

    }

    @Override
    public void sendAuthorityHeartbeat(AppendEntries heartbeat) {

    }

    @Override
    public void sendHeartbeat(AppendEntries heartbeat, String to) {

    }

    @Override
    public void sendAppendEntries(AppendEntries appendEntries, String to) {

    }

    @Override
    public void newClientRequest() {

    }

    @Override
    public void newFollowerState() {

    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

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

            reply = (RequestVoteReply) this.sendRPCHandler(() -> this.outbound.requestVote(this.targetServerName, requestVote));

        } while (reply == null);

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
    private Message sendRPCHandler(Callable<? extends Message> rpc) {

        long start = System.currentTimeMillis();

        try {

            return rpc.call();

        } catch (ExecutionException e) {
            // If target server is not alive

            log.warn("Server " + this.targetServerName + " is not up!!");

            // sleep for the remaining time, if any
            try {
                Thread.sleep(this.raftProperties.getHeartbeat().toMillis() - (System.currentTimeMillis() - start));
            } catch (Exception ignored) {}

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
