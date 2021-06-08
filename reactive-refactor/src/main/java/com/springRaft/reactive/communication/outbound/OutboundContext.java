package com.springRaft.reactive.communication.outbound;

import com.springRaft.reactive.communication.message.AppendEntries;
import com.springRaft.reactive.communication.message.AppendEntriesReply;
import com.springRaft.reactive.communication.message.RequestVote;
import com.springRaft.reactive.communication.message.RequestVoteReply;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    public Mono<AppendEntriesReply> appendEntries(String to, AppendEntries message) {
        return this.communicationStrategy.appendEntries(to, message);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(String to, RequestVote message) {
        return this.communicationStrategy.requestVote(to,message);
    }

    @Override
    public Mono<?> request(String command, String location) {
        return this.communicationStrategy.request(command, location);
    }
}
