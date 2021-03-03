package com.springRaft.servlet.consensusModule;

import com.springRaft.servlet.communication.message.Message;
import com.springRaft.servlet.communication.message.RequestVote;
import com.springRaft.servlet.communication.message.RequestVoteReply;
import com.springRaft.servlet.persistence.state.StateService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
@AllArgsConstructor
public class Leader implements RaftState {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(Leader.class);

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Service to access persisted state repository */
    private final StateService stateService;

    /* --------------------------------------------------- */

    @Override
    public void appendEntries() {

    }

    @Override
    public RequestVoteReply requestVote(RequestVote requestVote) {

        RequestVoteReply reply = this.applicationContext.getBean(RequestVoteReply.class);
        reply.setTerm(this.stateService.getCurrentTerm());
        reply.setVoteGranted(false);

        return reply;
    }

    @Override
    public void requestVoteReply(RequestVoteReply requestVoteReply) {

    }

    @Override
    public void work() {

        log.info("LEADER");



    }

    @Override
    public Message getNextMessage(String to) {
        return null;
    }

}
