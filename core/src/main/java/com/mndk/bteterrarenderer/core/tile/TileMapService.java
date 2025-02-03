package com.mndk.bteterrarenderer.core.tile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.core.config.registry.TileMapServiceParseRegistries;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.BufferBuildersManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.VertexBeginner;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.i18n.Translatable;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@JsonSerialize(using = TileMapService.Serializer.class)
@JsonDeserialize(using = TileMapService.Deserializer.class)
public interface TileMapService extends AutoCloseable {

    static CategoryMap.Wrapper<TileMapService> getSelected() { return TileMapServiceSelection.get(); }
    static void selectForDisplay(CategoryMap.Wrapper<TileMapService> wrapper) { TileMapServiceSelection.set(wrapper); }
    static void refreshSelectionFromConfig() { TileMapServiceSelection.refresh(); }

    Translatable<String> getName();
    Translatable<String> getCopyrightTextJson();
    URL getIconUrl();

    List<PropertyAccessor.Localized<?>> getStateAccessors();
    void moveAlongYAxis(double amount);

    VertexBeginner getVertexBeginner(BufferBuildersManager manager, float opacity);
    List<GraphicsModel> getModels(McCoord cameraPos, double yawDegrees, double pitchDegrees);
    McCoordTransformer getModelPositionTransformer();
    void cleanUp();

    void renderHud(GuiDrawContextWrapper context);

    class Serializer extends JsonSerializer<TileMapService> {
        @Override
        public void serialize(TileMapService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Class<? extends TileMapService> clazz = BTRUtil.uncheckedCast(value.getClass());
            String type = TileMapServiceParseRegistries.TYPE_MAP.inverse().get(clazz);
            if (type == null) {
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
            if (clazz == null) {
                throw JsonMappingException.from(p, "unknown map type: " + type);
            }

            return ctxt.readTreeAsValue(node, clazz);
        }
    }
}
