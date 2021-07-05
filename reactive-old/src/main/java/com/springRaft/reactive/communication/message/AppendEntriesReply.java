package com.springRaft.reactive.communication.message;

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
public class AppendEntriesReply implements Message {

    /* CurrentTerm, for leader to update itself */
    private Long term;

    /* True if follower contained entry matching prevLogIndex and prevLogTerm */
    private Boolean success;

}
