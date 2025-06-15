package com.mndk.bteterrarenderer.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = JsonStringSerializer.class)
@JsonDeserialize(using = JsonStringDeserializer.class)
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
}
