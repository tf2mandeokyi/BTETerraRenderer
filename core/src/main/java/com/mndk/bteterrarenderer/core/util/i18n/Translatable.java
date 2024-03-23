package com.mndk.bteterrarenderer.core.util.i18n;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = Translatable.Serializer.class)
@JsonDeserialize(using = Translatable.Deserializer.class)
public class Translatable<T> {
    public static final String DEFAULT_KEY = "en_us";

    private final Map<String, T> translations;

    public T get() {
        return this.get(McConnector.common().i18nManager.getCurrentLanguage());
    }
    private T get(String language) {
        return Optional.ofNullable(translations.get(language)).orElse(translations.get(DEFAULT_KEY));
    }
    public <U> Translatable<U> map(Function<T, U> function) {
        Map<String, U> newMap = new HashMap<>();
        translations.forEach((key, value) -> newMap.put(key, function.apply(value)));
        return new Translatable<>(newMap);
    }

    public static class Serializer extends JsonSerializer<Translatable<?>> {
        @Override
        public void serialize(Translatable<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            for(Map.Entry<String, ?> entry : value.translations.entrySet()) {
                gen.writeObjectField(entry.getKey(), entry.getValue());
            }
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Translatable<?>> implements ContextualDeserializer {
        private JavaType valueType;

        @Override
        public Translatable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_OBJECT) {
                p.nextToken();
            }
            JsonNode node = ctxt.readTree(p);

            // Try map object
            try {
                if(!node.isObject()) throw JsonMappingException.from(p, "");

                Map<String, Object> translations = new HashMap<>();
                for(Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                    String fieldName = it.next();
                    Object fieldValue = ctxt.readTreeAsValue(node.get(fieldName), valueType);
                    translations.put(fieldName, fieldValue);
                }
                if(!translations.containsKey(DEFAULT_KEY)) {
                    // If the value for default key doesn't exist, it will pick the first entry
                    // as the default key.
                    String alternativeKey = new ArrayList<>(translations.keySet()).get(0);
                    translations.put(DEFAULT_KEY, translations.get(alternativeKey));
                }
                return new Translatable<>(translations);
            } catch(IOException ignored) {}

            // If the serialization fails, try default object
            Object defaultValue = ctxt.readTreeAsValue(node, valueType);
            Map<String, Object> translations = new HashMap<String, Object>() {{ put(DEFAULT_KEY, defaultValue); }};
            return new Translatable<>(translations);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
            if(wrapperType == null) return new Deserializer();

            JavaType valueType = wrapperType.containedType(0);
            Deserializer deserializer = new Deserializer();
            deserializer.valueType = valueType;
            return deserializer;
        }
    }
}
