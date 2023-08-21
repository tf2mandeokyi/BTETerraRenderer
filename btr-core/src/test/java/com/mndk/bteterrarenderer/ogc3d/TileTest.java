package com.mndk.bteterrarenderer.ogc3d;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.ogc3d.math.Matrix4;
import com.mndk.bteterrarenderer.core.ogc3d.math.volume.Region;
import com.mndk.bteterrarenderer.core.ogc3d.tile.Tile;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

public class TileTest {

    @Test
    public void givenTileJsonWithASingleContent_testReadability() throws JsonProcessingException {
        String json = "{\n" +
                "  \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "  \"geometricError\": 43.88464075650763,\n" +
                "  \"refine\" : \"ADD\",\n" +
                "  \"content\": {\n" +
                "    \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "    \"uri\": \"2/0/0.glb\"\n" +
                "  },\n" +
                "  \"children\": []\n" +
                "}";

        Tile tile = BTETerraRendererConstants.JSON_MAPPER.readValue(json, Tile.class);
        MatcherAssert.assertThat(tile.getBoundingVolume(), CoreMatchers.instanceOf(Region.class));
        Assert.assertEquals(tile.getContents().size(), 1);
        Assert.assertEquals(tile.getTransform(), new Matrix4((r, c) -> r == c ? 1 : 0));
    }

    @Test
    public void givenTileJsonWithMultipleContents_testReadability() throws JsonProcessingException {
        String json = "{\n" +
                "  \"transform\": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15],\n" +
                "  \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "  \"geometricError\": 43.88464075650763,\n" +
                "  \"refine\" : \"ADD\",\n" +
                "  \"contents\": [{\n" +
                "    \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "    \"uri\": \"2/0/0.glb\",\n" +
                "    \"group\": 0\n" +
                "  }, {\n" +
                "    \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "    \"uri\": \"2/0/0.glb\",\n" +
                "    \"group\": 0\n" +
                "  }],\n" +
                "  \"children\": []\n" +
                "}";

        Tile tile = BTETerraRendererConstants.JSON_MAPPER.readValue(json, Tile.class);
        MatcherAssert.assertThat(tile.getBoundingVolume(), CoreMatchers.instanceOf(Region.class));
        Assert.assertEquals(tile.getContents().size(), 2);
        Assert.assertEquals(tile.getTransform(), new Matrix4((r, c) -> c*4+r));
    }
}
