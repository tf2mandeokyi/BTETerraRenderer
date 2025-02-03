package com.mndk.bteterrarenderer.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.io.IOException;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = JsonString.Serializer.class)
@JsonDeserialize(using = JsonString.Deserializer.class)
public class JsonString {
    @Nullable
    private final String value;

    public static JsonString from(String json) throws JsonProcessingException {
        BTETerraRenderer.JSON_MAPPER.readTree(json);
        return new JsonString(json);
    }

    public static JsonString fromUnsafe(String json) {
        try { return JsonString.from(json); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    public static JsonString from(JsonNode node) {
        return new JsonString(node.toString());
    }

    static class Serializer extends JsonSerializer<JsonString> {
        @Override
        public void serialize(JsonString value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            JsonNode node = BTETerraRenderer.JSON_MAPPER.readTree(value.value);
            gen.writeTree(node);
        }
    }

    static class Deserializer extends JsonDeserializer<JsonString> {
        @Override
        public JsonString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_OBJECT) {
                p.nextToken();
            }
            return JsonString.from(ctxt.readTree(p));
        }
    }
}
