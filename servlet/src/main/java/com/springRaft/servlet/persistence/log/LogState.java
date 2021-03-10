package com.springRaft.servlet.persistence.log;

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
public class LogState {

    /* Id of the object */
    @Id
    @GeneratedValue
    private Long id;

    /* Index of the highest log entry known to be committed */
    private Long committedIndex;

    /* Term of the highest log entry known to be committed */
    private Long committedTerm;

    /* Index of the highest log entry applied to state machine */
    private Long lastApplied;

}
