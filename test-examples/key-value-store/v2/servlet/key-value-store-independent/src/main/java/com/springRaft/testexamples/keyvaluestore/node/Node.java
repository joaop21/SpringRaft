package com.springRaft.testexamples.keyvaluestore.node;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Node {

    private Long createdIndex;

    private String key;

    private String value;

}
