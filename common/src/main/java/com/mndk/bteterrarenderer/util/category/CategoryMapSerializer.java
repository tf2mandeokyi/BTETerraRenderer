package com.mndk.bteterrarenderer.util.category;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

class CategoryMapSerializer extends JsonSerializer<CategoryMap<Object>> {
    @Override
    public void serialize(CategoryMap<Object> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject(); // main

        map.forEachThrowable((categoryName, category) -> {
            gen.writeFieldName(categoryName);
            gen.writeStartObject();

            category.forEach((id, value) -> {
                gen.writeFieldName(id);
                gen.writeObject(value);
            });
            gen.writeEndObject();
        });

        gen.writeEndObject(); // main
    }
}
