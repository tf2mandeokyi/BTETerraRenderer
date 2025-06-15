package com.mndk.bteterrarenderer.mcconnector.i18n;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

class TranslatableSerializer extends JsonSerializer<Translatable<?>> {
    @Override
    public void serialize(Translatable<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, ?> entry : value.getTranslations().entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }
}
