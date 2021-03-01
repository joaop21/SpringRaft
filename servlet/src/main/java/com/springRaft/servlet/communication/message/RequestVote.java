package com.springRaft.servlet.communication.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
@Getter
public class RequestVote implements Message {

    /* Candidate's term */
    private final Long term;

    /* Candidate requesting vote */
    private final String candidateID;

    /* Index of candidate's last log entry */
    private final Long lastLogIndex;

    /* Term of candidate's last log entry */
    private final Long lastLogTerm;

}
