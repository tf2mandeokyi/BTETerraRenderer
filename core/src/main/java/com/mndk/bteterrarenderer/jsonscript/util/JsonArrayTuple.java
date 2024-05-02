package com.mndk.bteterrarenderer.jsonscript.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonArrayTuple {

    private final Object[] array;

    public <T> T get(int index) {
        return BTRUtil.uncheckedCast(array[index]);
    }

    public static ParserReaderBuilder parserReaderBuilder() {
        return new ParserReaderBuilder(new ArrayList<>());
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParserReaderBuilder {
        private final List<Object> types;

        public ParserReaderBuilder next(Class<?> clazz) {
            types.add(clazz);
            return this;
        }

        public ParserReaderBuilder next(JavaType type) {
            types.add(type);
            return this;
        }

        public ParserReader build() {
            return new ParserReader(this.types.toArray(new Object[0]));
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParserReader implements JsonParserReader<JsonArrayTuple> {
        private final Object[] types;

        public JsonArrayTuple read(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<JsonNode> nodes = JsonParserUtil.readJsonList(p, p1 -> ctxt.readTree(p));
            if(nodes.size() != types.length) {
                throw JsonMappingException.from(p, "array size should be " + types.length +
                        ", instead was " + nodes.size());
            }

            Object[] result = new Object[types.length];
            for(int i = 0; i < types.length; i++) {
                if(types[i] instanceof Class<?>) {
                    result[i] = ctxt.readTreeAsValue(nodes.get(i), (Class<?>) types[i]);
                }
                else {
                    result[i] = ctxt.readTreeAsValue(nodes.get(i), (JavaType) types[i]);
                }
            }

            return new JsonArrayTuple(result);
        }
    }
}
