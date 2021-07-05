package com.springraft.persistencejpa.state;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Component
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class State extends com.springraft.persistence.state.State {

    @Override
    @Id
    public Long getId() {
        return super.getId();
    }

}
