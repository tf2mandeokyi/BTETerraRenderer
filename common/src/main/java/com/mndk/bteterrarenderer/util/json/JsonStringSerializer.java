package com.mndk.bteterrarenderer.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mndk.bteterrarenderer.BTETerraRenderer;

import java.io.IOException;

class JsonStringSerializer extends JsonSerializer<JsonString> {
    @Override
    public void serialize(JsonString value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonNode node = BTETerraRenderer.JSON_MAPPER.readTree(value.getValue());
        gen.writeTree(node);
    }
}
