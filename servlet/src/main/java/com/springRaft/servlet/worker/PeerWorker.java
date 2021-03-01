package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.outbound.MessageSubscriber;
import com.springRaft.servlet.communication.outbound.OutboundContext;
import com.springRaft.servlet.consensusModule.RaftState;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")
public class PeerWorker implements Runnable, MessageSubscriber {

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Address of the target server */
    private final InetSocketAddress targetServer;

    /* String of the address of the target server */
    private final String targetServerName;

    /* Mutex for some operations */
    private final Lock lock;

    /* Condition where a thread can wait for new messages */
    private final Condition newMessagesCondition;

    /* --------------------------------------------------- */

    public PeerWorker(
            OutboundContext outbound,
            InetSocketAddress targetServer
    ) {
        this.outbound = outbound;
        this.targetServer = targetServer;
        this.targetServerName = targetServer.getHostName() + ":" + targetServer.getPort();
        this.lock = new ReentrantLock();
        this.newMessagesCondition = lock.newCondition();
    }

    /* --------------------------------------------------- */

    @Override
    public void run() {

    }

    /* --------------------------------------------------- */

    /**
     * TODO
     * */
    @Override
    public void newMessage(RaftState state) {
        lock.lock();
        try {
            this.newMessagesCondition.signal();
        } finally {
            lock.unlock();
        }
    }
}
