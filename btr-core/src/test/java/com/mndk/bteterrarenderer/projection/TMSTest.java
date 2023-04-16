package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.loader.CategoryMapData;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import com.mndk.bteterrarenderer.tile.TileMapService;
import org.junit.Assert;
import org.junit.Test;

public class TMSTest {
    private static final CategoryMapData<TileMapService> CATEGORY_MAP_DATA;
    private final double longitude = 126.97683816936377, latitude = 37.57593302824052;

    @Test
    public void givenYamlConfig_testJacksonReadability() {
        Assert.assertNotNull(CATEGORY_MAP_DATA.getCategory("Global"));
    }

    @Test
    public void givenYamlConfig_testOsmUrl() throws OutOfProjectionBoundsException {
        TileMapService osm = CATEGORY_MAP_DATA.getCategory("Global").get("osm");
        Assert.assertTrue(osm.getUrlFromGeoCoordinate(longitude, latitude, 1).matches(
                "https://[abc]\\.tile\\.openstreetmap\\.org/19/447067/203014\\.png"
        ));
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
