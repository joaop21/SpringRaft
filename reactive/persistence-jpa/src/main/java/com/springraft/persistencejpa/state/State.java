package com.springraft.persistencejpa.state;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Component
@NoArgsConstructor
@Getter
@Setter
@ToString
public class State extends com.springraft.persistence.state.StateModel {

    @Override
    @Id
    public Long getId() {
        return super.getId();
    }

}
