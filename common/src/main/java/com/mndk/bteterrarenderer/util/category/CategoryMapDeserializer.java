package com.mndk.bteterrarenderer.util.category;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

class CategoryMapDeserializer extends JsonDeserializer<CategoryMap<?>> implements ContextualDeserializer {
    private JavaType valueType;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType categoryMapType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType valueType = categoryMapType.containedType(0);
        CategoryMapDeserializer deserializer = new CategoryMapDeserializer();
        deserializer.valueType = valueType;
        return deserializer;
    }

    @Override
    public CategoryMap<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.START_OBJECT)
            p.nextToken();

        JsonNode node = ctxt.readTree(p);
        CategoryMap<Object> result = new CategoryMap<>();

        for (Iterator<Map.Entry<String, JsonNode>> categoryIt = node.fields(); categoryIt.hasNext(); ) {
            Map.Entry<String, JsonNode> categoryEntry = categoryIt.next();
            String categoryName = categoryEntry.getKey();
            JsonNode categoryNode = categoryEntry.getValue();

            if (!categoryNode.isObject())
                throw JsonMappingException.from(p, "category should be an object");

            Category<Object> category = new Category<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = categoryNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> valueEntry = it.next();
                String valueId = valueEntry.getKey();
                category.put(valueId, ctxt.readTreeAsValue(valueEntry.getValue(), this.valueType));
            }

            result.getMap().put(categoryName, category);
        }

        return result;
    }
}
