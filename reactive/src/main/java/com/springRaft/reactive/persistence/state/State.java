package com.springRaft.reactive.persistence.state;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class State {

    /* Id of the object */
    @Id
    private Long id;

    /* Value for the current term */
    private Long currentTerm;

    /* Who the vote was for in the current term */
    private String votedFor;

}
