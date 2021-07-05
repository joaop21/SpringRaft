package com.springraft.persistencejpa.log;

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
public class LogState extends com.springraft.persistence.log.LogStateModel {

    @Override
    @Id
    public Long getId() {
        return super.getId();
    }

}
