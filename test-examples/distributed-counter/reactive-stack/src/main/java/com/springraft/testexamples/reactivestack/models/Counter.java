package com.springraft.testexamples.reactivestack.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Counter {

    /*--------------------------------------------------------------------------------*/

    /* Id of the object */
    @Id
    private long id;

    /* Value of the counter */
    private long value;

    /* Version of the counter (Optimistic Locking) */
    @Version
    private long version;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public void setValue(long value) {
        this.value = value;
    }

}
