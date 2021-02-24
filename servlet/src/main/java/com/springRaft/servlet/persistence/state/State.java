package com.springRaft.servlet.persistence.state;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class State {

    /* Id of the object */
    @Id
    @GeneratedValue
    private Long id;

    /* Value for the current term */
    private Long currentTerm;

    /* Who the vote was for in the current term */
    private String votedFor;

}
