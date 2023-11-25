package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.loader.FlatTileProjectionYamlLoader;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjection;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class FlatTileProjectionTest {
    private static final Map<String, FlatTileProjectionImpl> PROJECTION_MAP;
    private final double longitude = 126.97683816936377, latitude = 37.57593302824052;

    @Test
    public void givenYamlConfig_testWebMercatorTransform() throws Exception {
        FlatTileProjection webMercator = PROJECTION_MAP.get("webmercator");
        Assert.assertArrayEquals(
                webMercator.toTileCoord(longitude, latitude, 21),
                new int[] { 1788269, 812057 }
        );
    }

    @Test
    public void givenYamlConfig_testKakaoProjectionTransform() throws Exception {
        FlatTileProjection kakaoProjection = PROJECTION_MAP.get("kakaoprojection");
        Assert.assertArrayEquals(
                kakaoProjection.toTileCoord(longitude, latitude, 1),
                new int[] { 3561, 8014 }
        );
    }

    static {
        try {
            BTETerraRendererConfig.initialize(new File("test"));
            FlatTileProjectionYamlLoader.INSTANCE.refresh();
            PROJECTION_MAP = FlatTileProjectionYamlLoader.INSTANCE.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
