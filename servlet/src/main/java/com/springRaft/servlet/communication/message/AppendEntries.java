package com.springRaft.servlet.communication.message;

import lombok.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AppendEntries implements Message {

    /* Leader's term */
    private Long term;

    /* So follower can redirect clients */
    private String leaderId;

    /* Index of log entry immediately preceding new ones */
    private Long prevLogIndex;

    /* Term of prevLogIndex entry */
    private Long prevLogTerm;

    /* Log entries to store (empty for heartbeat; may send more than one for efficiency) */
    // IT SHOULD NOT BE STRING BUT ENTRY THAT DOESNT EXIST YET
    // ...
    private List<String> entries;

    /* Leader’s commitIndex */
    private Long leaderCommit;

}
