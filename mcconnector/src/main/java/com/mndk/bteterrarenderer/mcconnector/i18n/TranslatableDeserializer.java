package com.mndk.bteterrarenderer.mcconnector.i18n;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class TranslatableDeserializer extends JsonDeserializer<Translatable<?>> implements ContextualDeserializer {
    private JavaType valueType;

    @Override
    public Translatable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }
        JsonNode node = ctxt.readTree(p);

        // Try map object
        try {
            if (!node.isObject()) throw JsonMappingException.from(p, "");

            Map<String, Object> translations = new HashMap<>();
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                Object fieldValue = ctxt.readTreeAsValue(node.get(fieldName), valueType);
                translations.put(fieldName, fieldValue);
            }
            if (!translations.containsKey(Translatable.DEFAULT_KEY)) {
                // If the value for default key doesn't exist, it will pick the first entry
                // as the default key.
                String alternativeKey = new ArrayList<>(translations.keySet()).get(0);
                translations.put(Translatable.DEFAULT_KEY, translations.get(alternativeKey));
            }
            return new Translatable<>(translations);
        } catch (IOException ignored) {
        }

        // If the serialization fails, try default object
        Object defaultValue = ctxt.readTreeAsValue(node, valueType);
        Map<String, Object> translations = new HashMap<String, Object>() {{
            put(Translatable.DEFAULT_KEY, defaultValue);
        }};
        return new Translatable<>(translations);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        if (wrapperType == null) return new TranslatableDeserializer();

        JavaType valueType = wrapperType.containedType(0);
        TranslatableDeserializer deserializer = new TranslatableDeserializer();
        deserializer.valueType = valueType;
        return deserializer;
    }
}
