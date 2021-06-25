package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.*;

public interface RaftInboundCommunication {

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of the appendEntriesRPC.
     *
     * @param appendEntries Message received in AppendEntriesRPC.
     *
     * @return AppendEntriesReply Reply to send to the server which invoke the communication.
     * */
    AppendEntriesReply appendEntries(AppendEntries appendEntries);

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of the requestVoteRPC.
     *
     * @param requestVote Message received in requestVoteRPC.
     *
     * @return RequestVoteReply Reply to send to the server which invoke the communication.
     * */
    RequestVoteReply requestVote(RequestVote requestVote);

}
