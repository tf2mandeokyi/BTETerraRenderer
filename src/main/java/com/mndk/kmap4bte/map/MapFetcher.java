package com.mndk.kmap4bte.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapFetcher {

    private static int domain_num = 0;

    private static final Map<String, ResourceLocation> resourceLocations = new HashMap<>();

    /**
     * Maximum size that resourceLocations can have.
     */
    @Deprecated
    private static final int MAX_SIZE = 100;

    /**
     * Fetches kakao map, and then returns it as ResourceLocation.
     * @param tileX
     * @param tileY
     * @param level
     * @param type
     * @return
     * @throws IOException
     */
    public static ResourceLocation getKakaoMapRLByTileIndex(int tileX, int tileY, int level, RenderMapType type) throws IOException {
        String tileID = genTileID(tileX, tileY, level, type);

        if(resourceLocations.containsKey(tileID)) return resourceLocations.get(tileID);

        BufferedImage image = fetchKakaoMapByTileIndex(tileX, tileY, level, type);

        ResourceLocation result = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(
                genTileID(tileX, tileY, level, type),
                new DynamicTexture(Objects.requireNonNull(image))
        );

        resourceLocations.put(tileID, result);

        return result;
    }



    @Deprecated
    private static ResourceLocation addResourceLocation(String id, BufferedImage image) {
        if(resourceLocations.containsKey(id)) {
            ResourceLocation newResult = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(id, new DynamicTexture(image));
            resourceLocations.put(id, newResult);
            return newResult;
        }
        if(resourceLocations.size() >= MAX_SIZE) resourceLocations.remove(0);
        ResourceLocation result = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(id, new DynamicTexture(image));
        resourceLocations.put(id, result);
        return result;
    }



    /**
     * Fetches kakao map, and then returns it as BufferedImage.
     * @param tileX
     * @param tileY
     * @param level
     * @param type
     * @return
     * @throws IOException
     */
    private static BufferedImage fetchKakaoMapByTileIndex(int tileX, int tileY, int level, RenderMapType type) throws IOException {

        URL url;
        String dir = type == RenderMapType.AERIAL ? "map_skyview" : "map_2d/2012tlq";
        String fileType = type == RenderMapType.AERIAL ? ".jpg" : ".png";

        try {
            url = new URL("http://map" + domain_num + ".daumcdn.net/" +
                    dir + "/L" + level + "/" + tileY + "/" + tileX + fileType
            );
        } catch (MalformedURLException e) {
            return null;
        }

        domain_num++;
        if(domain_num >= 4) domain_num = 0;

        return ImageIO.read(url);
    }

    private static String genTileID(int tileX, int tileY, int level, RenderMapType type) {
        return "tilemap_" + tileX + "_" + tileY + "_" + level + "_" + type.toString();
    }

}
