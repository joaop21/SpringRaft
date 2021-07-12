package com.springraft.raft.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.springraft.persistence.log.Entry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class EntryDeserializer extends JsonDeserializer<Entry> {

    private final Entry implementation;

    /* --------------------------------------------------- */

    public EntryDeserializer(@Qualifier("EntryZero") Entry implementation) {
        this.implementation = implementation;
    }

    /* --------------------------------------------------- */

    @Override
    public Entry deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return jsonParser.readValueAs(implementation.getClass());
    }
}
