package com.springRaft.servlet.communication.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@AllArgsConstructor
@Getter
public class RequestVoteReply implements Message {

    /* Current term, for candidate to update itself */
    private final Long term;

    /* True means candidate received vote */
    private final Boolean voteGranted;

}
