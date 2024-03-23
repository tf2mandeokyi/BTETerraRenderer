package com.mndk.bteterrarenderer.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjection;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.EmptyClientMinecraftManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.mndk.bteterrarenderer.core.projection.FlatTileProjectionTest.*;

public class TileMapServiceJsonCopyTest {

    @Test
    public void givenProjectionYamlConfig_whenJsonCopy_testSameCoord() throws OutOfProjectionBoundsException, JsonProcessingException {
        FlatTileProjection webMercator = PROJECTION_MAP.get("webmercator");
        int[] coord1 = webMercator.toTileCoord(LONGITUDE, LATITUDE, 21);

        String json = BTETerraRendererConstants.JSON_MAPPER.writeValueAsString(webMercator);

        FlatTileProjection projectionCopy = BTETerraRendererConstants.JSON_MAPPER.readValue(json, FlatTileProjectionImpl.class);
        int[] coord2 = projectionCopy.toTileCoord(LONGITUDE, LATITUDE, 21);

        Assert.assertArrayEquals(coord1, coord2);
    }

    @Test
    public void givenTMSConfig_whenTileCoordProvided_testSameCoord() throws OutOfProjectionBoundsException, IOException {
        FlatTileMapService tms = (FlatTileMapService) TileMapServiceYamlLoader.INSTANCE.getResult()
                .getItem("Global", "osm");
        Assert.assertNotNull(tms);
        int[] coord1 = tms.getCoordTranslator().getProjection().toTileCoord(LONGITUDE, LATITUDE, 21);
        int[] coord2 = tms.getCoordTranslator().geoCoordToTileCoord(LONGITUDE, LATITUDE, 0);

        String json = BTETerraRendererConstants.JSON_MAPPER.writeValueAsString(tms);

        JsonNode node = BTETerraRendererConstants.JSON_MAPPER.readTree(json);
        Assert.assertEquals("webmercator", node.get("projection").asText());

        FlatTileMapService tmsCopy = BTETerraRendererConstants.JSON_MAPPER.readValue(json, FlatTileMapService.class);
        int[] coord3 = tmsCopy.getCoordTranslator().getProjection().toTileCoord(LONGITUDE, LATITUDE, 21);
        int[] coord4 = tmsCopy.getCoordTranslator().geoCoordToTileCoord(LONGITUDE, LATITUDE, 0);
        tmsCopy.close();

        Assert.assertArrayEquals(coord1, coord3);
        Assert.assertArrayEquals(coord2, coord4);
    }

    static {
        BTETerraRendererConfig.initialize(new EmptyClientMinecraftManager(new File("test")));
    }

}
