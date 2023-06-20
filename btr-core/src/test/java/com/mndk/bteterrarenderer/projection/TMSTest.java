package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.loader.CategoryMap;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.FlatTileMapService;
import com.mndk.bteterrarenderer.tile.TileMapService;
import org.junit.Assert;
import org.junit.Test;

public class TMSTest {
    private static final CategoryMap<TileMapService> CATEGORY_MAP_DATA;

    @Test
    public void givenYamlConfig_testJacksonReadability() {
        Assert.assertNotNull(CATEGORY_MAP_DATA.get("Global"));
    }

    @Test
    public void givenYamlConfig_testOsmUrl() throws OutOfProjectionBoundsException {
        FlatTileMapService osm = (FlatTileMapService) CATEGORY_MAP_DATA.get("Global").get("osm")
                .getValue();
        double longitude = 126.97683816936377, latitude = 37.57593302824052;

        Assert.assertTrue(osm.getUrlFromGeoCoordinate(longitude, latitude, 1).matches(
                "https://[abc]\\.tile\\.openstreetmap\\.org/19/447067/203014\\.png"
        ));
    }

    @Test
    public void givenYamlConfig_testCategory() {
        CategoryMap.Wrapper<TileMapService> osm = CATEGORY_MAP_DATA.get("Global").get("osm");

        Assert.assertEquals("Global", osm.getParentCategory().getName());
        Assert.assertEquals("default", osm.getSource());
    }

    static {
        try {
            ProjectionYamlLoader.INSTANCE.refresh(); // This should be called first
            TMSYamlLoader.INSTANCE.refresh();
            CATEGORY_MAP_DATA = TMSYamlLoader.INSTANCE.result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
