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
public class RequestReply implements Message {

    /* Success of the request */
    private Boolean success;

    /* Result of applying a command to the state machine */
    private Object response;

    /* If this server is not the leader we have to redirect the request */
    private Boolean redirect;

    /* Where to redirect the request */
    private String redirectTo;

}