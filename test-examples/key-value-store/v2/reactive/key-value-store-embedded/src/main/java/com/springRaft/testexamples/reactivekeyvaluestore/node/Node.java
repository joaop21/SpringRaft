package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Node implements Persistable<Long> {

    @Id
    private Long createdIndex;

    @Column
    @NonNull
    private String key;

    private String value;

    /* Transient flag that states ith the object is new or already in database */
    @Transient
    private boolean isNew;

    /* --------------------------------------------------- */

    public Node(String key, String value) {
        this.key = key;
        this.value = value;
        this.isNew = true;
    }

    /* --------------------------------------------------- */

    @Override
    public Long getId() {
        return this.createdIndex;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
