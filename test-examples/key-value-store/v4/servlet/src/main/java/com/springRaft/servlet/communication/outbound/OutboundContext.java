package com.springRaft.servlet.communication.outbound;

import com.springRaft.servlet.communication.message.AppendEntries;
import com.springRaft.servlet.communication.message.AppendEntriesReply;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Scope("singleton")
@NoArgsConstructor
public class OutboundContext implements OutboundStrategy {

    /* Outbound communication Strategy to use */
    private OutboundStrategy communicationStrategy = null;

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

    @Override
    public Object request(String command, String location) throws InterruptedException, ExecutionException, URISyntaxException {
        return this.communicationStrategy.request(command, location);
    }

}