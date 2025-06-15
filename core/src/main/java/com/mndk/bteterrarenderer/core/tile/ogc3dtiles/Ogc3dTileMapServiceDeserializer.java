package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceCommonProperties;
import com.mndk.bteterrarenderer.core.tile.TileMapServiceDeserializer;
import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.geoid.GeoidHeightFunction;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.util.json.JsonParserUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class Ogc3dTileMapServiceDeserializer extends TileMapServiceDeserializer<Ogc3dTileMapService> {
    @Override
    protected Ogc3dTileMapService deserialize(JsonNode node, TileMapServiceCommonProperties properties, DeserializationContext ctxt) throws IOException {
        double semiMajorAxis = JsonParserUtil.getOrDefault(node, "semi_major", Wgs84Constants.SEMI_MAJOR_AXIS);
        double semiMinorAxis = JsonParserUtil.getOrDefault(node, "semi_minor", Wgs84Constants.SEMI_MINOR_AXIS);
        String geoidType = JsonParserUtil.getOrDefault(node, "geoid", "wgs84");
        GeoidHeightFunction function = getGeoidHeightFunction(geoidType);
        SpheroidCoordinatesConverter coordConverter = new SpheroidCoordinatesConverter(semiMajorAxis, semiMinorAxis, function);

        boolean rotateModelAlongXAxis = JsonParserUtil.getOrDefault(node, "rotate_model_x", false);
        return Ogc3dTileMapService.builder()
                .properties(properties)
                .coordConverter(coordConverter)
                .rotateModelAlongEarthXAxis(rotateModelAlongXAxis)
                .geoidType(geoidType)
                .build();
    }

    @NotNull
    private static GeoidHeightFunction getGeoidHeightFunction(String geoidType) throws IOException {
        switch (geoidType) {
            case "wgs84": return GeoidHeightFunction.WGS84_ELLIPSOID;
            case "egm96": return GeoidHeightFunction.EGM96_WW15MGH;
            default: throw new IOException("Unknown geoid type: " + geoidType);
        }
    }
}
