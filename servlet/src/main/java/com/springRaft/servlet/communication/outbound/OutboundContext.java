package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Scope("singleton")
public class OutboundContext implements OutboundStrategy {

    /* Outbound communication Strategy to use */
    private OutboundStrategy communicationStrategy;

    /* --------------------------------------------------- */

    public OutboundContext(ApplicationContext applicationContext) {
        // REST is the default strategy for outbound communication
        this.communicationStrategy = applicationContext.getBean(REST.class);
    }

    /* --------------------------------------------------- */

    public void setCommunicationStrategy(OutboundStrategy communication) {
        this.communicationStrategy = communication;
    }

    /* --------------------------------------------------- */

    @Override
    public AppendEntriesReply appendEntries(String to, AppendEntries message) throws InterruptedException, ExecutionException, TimeoutException {
        return this.communicationStrategy.appendEntries(to, message);
    }

    @Override
    public RequestVoteReply requestVote(String to, RequestVote message) throws InterruptedException, ExecutionException, TimeoutException {
        return this.communicationStrategy.requestVote(to, message);
    }

}
