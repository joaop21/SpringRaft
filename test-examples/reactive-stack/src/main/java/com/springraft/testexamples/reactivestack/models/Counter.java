package com.springraft.testexamples.reactivestack.models;

import lombok.*;
import org.springframework.data.annotation.Id;

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

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    public void setValue(long value) {
        this.value = value;
    }

}
