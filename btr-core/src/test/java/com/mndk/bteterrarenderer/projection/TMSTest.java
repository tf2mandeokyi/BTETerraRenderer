package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.core.loader.CategoryMap;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.core.tile.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Test;

public class TMSTest {
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
        int[] tileCoord = osm.getTileProjection().geoCoordToTileCoord(longitude, latitude, 1);
        Assert.assertTrue(osm.getUrlFromTileCoordinate(tileCoord[0], tileCoord[1], 1).matches(
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
            BTETerraRendererConstants.LOGGER = LogManager.getLogger(BTETerraRendererConstants.class);
            ConfigLoaders.loadAll();
            CATEGORY_MAP_DATA = TMSYamlLoader.INSTANCE.result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
