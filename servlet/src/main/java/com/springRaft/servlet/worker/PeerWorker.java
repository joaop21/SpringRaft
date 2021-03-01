package com.springRaft.servlet.worker;

import com.springRaft.servlet.communication.outbound.OutboundContext;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Scope("prototype")
@AllArgsConstructor
public class PeerWorker implements Runnable {

    /* Outbound context for communication to other servers */
    private final OutboundContext outbound;

    /* Address of the target server */
    private final InetSocketAddress targetServer;

    /* --------------------------------------------------- */

    @Override
    public void run() {

    }

}
