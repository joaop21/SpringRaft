package com.springraft.raft.communication.message;

import lombok.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RequestVote implements Message {

    /* Candidate's term */
    private Long term;

    /* Candidate requesting vote */
    private String candidateId;

    /* Index of candidate's last log entry */
    private Long lastLogIndex;

    /* Term of candidate's last log entry */
    private Long lastLogTerm;

}
