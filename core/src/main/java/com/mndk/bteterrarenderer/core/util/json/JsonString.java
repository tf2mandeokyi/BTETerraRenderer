package com.mndk.bteterrarenderer.core.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = JsonString.Deserializer.class)
public class JsonString {
    @Nullable
    private final String value;

    public static class Deserializer extends JsonDeserializer<JsonString> {
        @Override
        public JsonString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_OBJECT) {
                p.nextToken();
            }

            JsonNode node = ctxt.readTree(p);
            if(node.isTextual()) return new JsonString(node.asText());
            else return new JsonString(node.toString());
        }
    }
}
