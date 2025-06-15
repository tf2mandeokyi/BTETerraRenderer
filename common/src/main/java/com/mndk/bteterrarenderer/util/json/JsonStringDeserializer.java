package com.mndk.bteterrarenderer.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

class JsonStringDeserializer extends JsonDeserializer<JsonString> {
    @Override
    public JsonString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }
        return JsonString.from(ctxt.readTree(p));
    }
}
