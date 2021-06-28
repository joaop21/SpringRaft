package com.springRaft.testexamples.keyvaluestoreindependent.node;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(
        indexes = { @Index(name = "key_index", columnList = "key", unique = true) }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long createdIndex;

    @Column(nullable = false)
    private String key;

    private String value;

    /* --------------------------------------------------- */

    public Node(String key, String value) {
        this.key = key;
        this.value = value;
    }

}
