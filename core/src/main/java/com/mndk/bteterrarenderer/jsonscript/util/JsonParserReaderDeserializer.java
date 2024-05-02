package com.mndk.bteterrarenderer.jsonscript.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class JsonParserReaderDeserializer<T> extends JsonDeserializer<T> {
    private final JsonParserReader<T> reader;

    @Override
    public final T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return reader.read(p, ctxt);
    }
}
