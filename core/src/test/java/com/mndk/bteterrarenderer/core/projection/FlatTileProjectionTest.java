package com.mndk.bteterrarenderer.core.projection;

import com.mndk.bteterrarenderer.core.BTETerraRendererCore;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjection;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import com.mndk.bteterrarenderer.mcconnector.TestEnvironmentVirtualMinecraftManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class FlatTileProjectionTest {
    public static final Map<String, FlatTileProjectionImpl> PROJECTION_MAP;
    // Gyeongbokgung, Seoul, South Korea
    public static final double LONGITUDE = 126.97683816936377, LATITUDE = 37.57593302824052;
    public static final int[] WEBMERCATOR_COORD = new int[] { 1788269, 812057 };
    public static final int[] KAKAOPROJECTION_COORD = new int[] { 3561, 8014 };

    @Test
    public void givenYamlConfig_testWebMercatorTransform() throws Exception {
        FlatTileProjection webMercator = PROJECTION_MAP.get("webmercator");
        Assert.assertArrayEquals(
                WEBMERCATOR_COORD,
                webMercator.toTileCoord(LONGITUDE, LATITUDE, 21)
        );
    }

    @Test
    public void givenYamlConfig_testKakaoProjectionTransform() throws Exception {
        FlatTileProjection kakaoProjection = PROJECTION_MAP.get("kakaoprojection");
        Assert.assertArrayEquals(
                KAKAOPROJECTION_COORD,
                kakaoProjection.toTileCoord(LONGITUDE, LATITUDE, 1)
        );
    }

    static {
        BTETerraRendererCore.initialize(TestEnvironmentVirtualMinecraftManager.getInstance());
        PROJECTION_MAP = ConfigLoaders.flatProj().getResult();
    }
}
