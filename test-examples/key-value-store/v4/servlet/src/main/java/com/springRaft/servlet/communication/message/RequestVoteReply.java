package com.springRaft.servlet.communication.message;

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
public class RequestVoteReply implements Message {

    /* Current term, for candidate to update itself */
    private Long term;

    /* True means candidate received vote */
    private Boolean voteGranted;

}
