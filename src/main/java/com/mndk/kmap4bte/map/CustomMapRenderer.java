package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.map.kakao.KakaoMapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class CustomMapRenderer {

    private static final Map<String, ResourceLocation> resourceLocations = new HashMap<>();

    private final RenderMapSource source;

    public static final CustomMapRenderer KAKAO = new KakaoMapRenderer();

    public CustomMapRenderer(RenderMapSource source) {
        this.source = source;
    }

    /**
     * Fetches kakao map, and then returns it as ResourceLocation.
     * @param playerX
     * @param playerZ
     * @param level
     * @param type
     * @return
     * @throws IOException
     */
    public ResourceLocation getMapResourceLocationByPlayerCoordinate(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int level, RenderMapType type
    ) throws IOException {

        String tileID = genTileID(0, 0, level, type, source);

        if(resourceLocations.containsKey(tileID)) return resourceLocations.get(tileID);

        BufferedImage image = this.fetchMap(playerX, playerZ, tileDeltaX, tileDeltaY, level, type);

        ResourceLocation result = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(
                genTileID(0, 0, level, type, source),
                new DynamicTexture(Objects.requireNonNull(image))
        );

        resourceLocations.put(tileID, result);

        return result;
    }



    public abstract BufferedImage fetchMap(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int level, RenderMapType type
    ) throws IOException;



    public abstract void renderTile(
            Tessellator t, BufferBuilder builder,    // Drawing components
            int level, RenderMapType type,           // Tile data
            double y,                                // rendering y position
            double opacity,                          // opacity
            double px, double py, double pz,         // Player position
            int tileDeltaX, int tileDeltaY     // Tile Delta
    ) throws IOException;



    private static String genTileID(int tileX, int tileY, int level, RenderMapType type, RenderMapSource source) {
        return "tilemap_" + source.getEnumName() + "_" + tileX + "_" + tileY + "_" + level + "_" + type.getEnumName();
    }

}
