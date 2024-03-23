package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileKey;
import com.mndk.bteterrarenderer.core.util.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.yml.TileMapServiceYamlLoader;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.mcconnector.client.EmptyClientMinecraftManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class TileMapServiceTest {
    private static final CategoryMap<TileMapService<?>> CATEGORY_MAP_DATA;

    @Test
    public void givenYamlConfig_testJacksonReadability() {
        Assert.assertNotNull(CATEGORY_MAP_DATA.getCategory("Global"));
    }

    @Test
    public void givenYamlConfig_testOsmUrl() throws OutOfProjectionBoundsException {
        FlatTileMapService osm = (FlatTileMapService) CATEGORY_MAP_DATA.getItem("Global", "osm");
        Assert.assertNotNull(osm);

        double longitude = 126.97683816936377, latitude = 37.57593302824052;
        int[] tileCoord = osm.getCoordTranslator().geoCoordToTileCoord(longitude, latitude, 1);
        FlatTileKey tileKey = new FlatTileKey(tileCoord[0], tileCoord[1], 1);
        Assert.assertTrue(osm.getUrlFromTileCoordinate(tileKey).matches(
                "https://[abc]\\.tile\\.openstreetmap\\.org/19/447067/203014\\.png"
        ));
    }

    @Test
    public void givenYamlConfig_testCategory() {
        CategoryMap.Wrapper<TileMapService<?>> osm = CATEGORY_MAP_DATA.getItemWrapper("Global", "osm");

        Assert.assertEquals("Global", osm.getParentCategory().getName());
        Assert.assertEquals("default", osm.getSource());
    }

    static {
        try {
            BTETerraRendererConfig.initialize(new EmptyClientMinecraftManager(new File("test")));
            CATEGORY_MAP_DATA = TileMapServiceYamlLoader.INSTANCE.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
