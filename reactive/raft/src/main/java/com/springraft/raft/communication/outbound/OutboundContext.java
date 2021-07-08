package com.springraft.raft.communication.outbound;

import com.springraft.raft.communication.message.AppendEntries;
import com.springraft.raft.communication.message.AppendEntriesReply;
import com.springraft.raft.communication.message.RequestVote;
import com.springraft.raft.communication.message.RequestVoteReply;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@NoArgsConstructor
public class OutboundContext implements OutboundStrategy {

    /* Outbound communication Strategy with cluster members */
    private OutboundStrategy clusterCommunicationStrategy = null;

    /* Outbound communication Strategy with Application Server */
    private OutboundStrategy applicationCommunicationStrategy = null;

    /* --------------------------------------------------- */

    public void setClusterCommunicationStrategy(OutboundStrategy communication) {
        this.clusterCommunicationStrategy = communication;
    }

    public void setApplicationCommunicationStrategy(OutboundStrategy communication) {
        this.applicationCommunicationStrategy = communication;
    }

    /* --------------------------------------------------- */

    @Override
    public Mono<AppendEntriesReply> appendEntries(String to, AppendEntries message) {
        return this.clusterCommunicationStrategy.appendEntries(to, message);
    }

    @Override
    public Mono<RequestVoteReply> requestVote(String to, RequestVote message) {
        return this.clusterCommunicationStrategy.requestVote(to,message);
    }

    @Override
    public Mono<?> request(String command, String location) {
        return this.applicationCommunicationStrategy.request(command, location);
    }
}
