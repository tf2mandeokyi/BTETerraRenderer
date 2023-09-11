package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.core.loader.ProjectionYamlLoader;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class TileProjectionTest {
    private static final Map<String, YamlTileProjection> PROJECTION_MAP;
    private final double longitude = 126.97683816936377, latitude = 37.57593302824052;

    @Test
    public void givenYamlConfig_testWebMercatorTransform() throws Exception {
        TileProjection webMercator = PROJECTION_MAP.get("webmercator");
        Assert.assertArrayEquals(
                webMercator.toTileCoord(longitude, latitude, 21),
                new int[] { 1788269, 812057 }
        );
    }

    @Test
    public void givenYamlConfig_testKakaoProjectionTransform() throws Exception {
        TileProjection kakaoProjection = PROJECTION_MAP.get("kakaoprojection");
        Assert.assertArrayEquals(
                kakaoProjection.toTileCoord(longitude, latitude, 1),
                new int[] { 3561, 8014 }
        );
    }

    static {
        try {
            Class.forName("com.mndk.bteterrarenderer.core.BTETerraRendererConstants");
            ProjectionYamlLoader.INSTANCE.refresh();
            PROJECTION_MAP = ProjectionYamlLoader.INSTANCE.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
