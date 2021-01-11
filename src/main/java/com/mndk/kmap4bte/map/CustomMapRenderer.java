package com.mndk.kmap4bte.map;

import com.mndk.kmap4bte.renderer.MapRenderer;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class CustomMapRenderer {

    private static final Map<String, ResourceLocation> resourceLocations = new HashMap<>();

    private final RenderMapSource source;

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
    ) throws IOException, OutOfProjectionBoundsException {

        int[] tileCoord = this.playerPositionToTileCoord(playerX, playerZ, level);

        String tileID = genTileID(tileCoord[0]+tileDeltaX, tileCoord[1]+tileDeltaY, level, type, source);

        if(resourceLocations.containsKey(tileID)) return resourceLocations.get(tileID);

        BufferedImage image = this.fetchMap(playerX, playerZ, tileDeltaX, tileDeltaY, level, type);

        ResourceLocation result = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(
                genTileID(0, 0, level, type, source),
                new DynamicTexture(Objects.requireNonNull(image))
        );

        resourceLocations.put(tileID, result);

        return result;
    }



    public abstract int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException;

    public abstract double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException;

    /**
     * This should return: [tileDeltaX, tileDeltaY, u, v]
     * @param i
     * @return
     */
    protected abstract int[] getCornerMatrix(int i);



    public abstract BufferedImage fetchMap(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int level, RenderMapType type
    ) throws IOException;



    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int level, RenderMapType type,
            double y, double opacity,
            double px, double py, double pz,
            int tileDeltaX, int tileDeltaY
    ) throws IOException {
        try {

            ResourceLocation resourceLocation = this.getMapResourceLocationByPlayerCoordinate(px, pz, tileDeltaX, tileDeltaY, level, type);

            int[] tilePos = this.playerPositionToTileCoord(px, pz, level);

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourceLocation);

            // begin vertex
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            double[] temp;

            // Convert boundaries
            for (int i = 0; i < 4; i++) {

                int[] mat = this.getCornerMatrix(i);
                temp = tileCoordToPlayerPosition(tilePos[0] + mat[0] + tileDeltaX, tilePos[1] + mat[1] + tileDeltaY, level);

                builder.pos(temp[0] - px, y - py, temp[1] - pz)
                            .tex(mat[2], mat[3])
                            .color(1.f, 1.f, 1.f, MapRenderer.opacity)
                            .endVertex();
            }

            t.draw();
        } catch(OutOfProjectionBoundsException ignored) {}
    }



    private static String genTileID(int tileX, int tileY, int level, RenderMapType type, RenderMapSource source) {
        return "tilemap_" + source.getEnumName() + "_" + tileX + "_" + tileY + "_" + level + "_" + type.getEnumName();
    }

}
