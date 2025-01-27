package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Region;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.Tile;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.joml.Matrix4d;
import org.joml.Vector3d;
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

        Tile tile = BTETerraRenderer.JSON_MAPPER.readValue(json, Tile.class);
        MatcherAssert.assertThat(tile.getBoundingVolume(), CoreMatchers.instanceOf(Region.class));
        Assert.assertEquals(1, tile.getContents().size());
        Assert.assertEquals(new Matrix4d(), tile.getGlobalTransform(new Matrix4d()));
    }

    @Test
    public void givenTileJsonWithMultipleContents_testReadability() throws JsonProcessingException {
        String json = "{\n" +
                "  \"transform\": [3, 0, 0, 0, 0, 2, 0, 0, 0, 0, 6, 0, 3, 4, 5, 1],\n" +
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
                "  \"children\": [" +
                "    {\n" +
                "      \"boundingVolume\": { \"region\": [0, 0, 0, 0, 0, 0] },\n" +
                "      \"geometricError\": 43.88464075650763,\n" +
                "      \"transform\": [4, 0, 0, 0, 0, 7, 0, 0, 0, 0, 2, 0, 6, 5, 2, 1],\n" +
                "      \"contents\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Tile tile = BTETerraRenderer.JSON_MAPPER.readValue(json, Tile.class);
        MatcherAssert.assertThat(tile.getBoundingVolume(), CoreMatchers.instanceOf(Region.class));
        Assert.assertEquals(2, tile.getContents().size());
        Assert.assertEquals(
                new Matrix4d().translate(new Vector3d(3, 4, 5)).scale(new Vector3d(3, 2, 6)),
                tile.getGlobalTransform(new Matrix4d())
        );

        Tile child = tile.getChildren().get(0);
        Assert.assertEquals(
                new Matrix4d().translate(new Vector3d(21, 14, 17)).scale(new Vector3d(12, 14, 12)),
                child.getGlobalTransform(new Matrix4d())
        );
    }
}
