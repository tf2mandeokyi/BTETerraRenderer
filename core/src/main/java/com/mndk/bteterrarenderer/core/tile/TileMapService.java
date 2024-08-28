package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.core.util.i18n.Translatable;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.List;

// TODO: Make copyright information appear on the UI
@JsonSerialize(using = TileMapService.Serializer.class)
@JsonDeserialize(using = TileMapService.Deserializer.class)
public interface TileMapService extends AutoCloseable {

    static CategoryMap.Wrapper<TileMapService> getSelected() {
        return TileMapServiceSelection.get();
    }

    static void selectForDisplay(CategoryMap.Wrapper<TileMapService> wrapper) {
        TileMapServiceSelection.set(wrapper);
    }

    static void refreshSelectionFromConfig() {
        TileMapServiceSelection.refresh();
    }

    Translatable<String> getName();
    Translatable<String> getCopyrightTextJson();
    URL getIconUrl();

    List<PropertyAccessor.Localized<?>> getStates();
    void moveAlongYAxis(double amount);

    void render(@Nonnull DrawContextWrapper<?> drawContextWrapper,
                double px, double py, double pz, float opacity);
    void cleanUp();

    class Serializer extends JsonSerializer<TileMapService> {
        @Override
        public void serialize(TileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Class<? extends TileMapService> clazz = BTRUtil.uncheckedCast(value.getClass());
            String type = TileMapServiceParseRegistries.TYPE_MAP.inverse().get(clazz);
            if(type == null) {
                throw JsonMappingException.from(gen, "unknown map class: " + clazz);
            }

            gen.writeObject(value);
        }
    }

    class Deserializer extends JsonDeserializer<TileMapService> {
        @Override
        public TileMapService deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);

            String type = JsonParserUtil.getOrDefault(node, "type", "flat");
            Class<? extends TileMapService> clazz = TileMapServiceParseRegistries.TYPE_MAP.get(type);
            if(clazz == null) {
                throw JsonMappingException.from(p, "unknown map type: " + type);
            }

            return ctxt.readTreeAsValue(node, clazz);
        }
    }
}
