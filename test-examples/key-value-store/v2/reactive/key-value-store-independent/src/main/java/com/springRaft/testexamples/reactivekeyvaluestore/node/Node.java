package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Node{

    private Long createdIndex;

    private String key;

    private String value;

}
