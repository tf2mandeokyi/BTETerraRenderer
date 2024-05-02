package com.mndk.bteterrarenderer.jsonscript.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.util.function.Function;

public interface JsonParserReader<T> {

    T read(JsonParser p, DeserializationContext ctxt) throws IOException;

    default <U> JsonParserReader<U> then(Function<T, U> function) {
        return (p, ctxt) -> {
            T result = this.read(p, ctxt);
            return function.apply(result);
        };
    }
}
