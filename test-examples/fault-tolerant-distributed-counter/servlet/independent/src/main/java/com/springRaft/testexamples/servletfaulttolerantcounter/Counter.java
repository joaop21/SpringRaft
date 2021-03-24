package com.springRaft.testexamples.servletfaulttolerantcounter;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Counter {

    @Id
    private Long id;

    @NonNull
    private Long value;

    public void increment() {
        this.value++;
    }

    public void decrement() {
        this.value--;
    }

}
