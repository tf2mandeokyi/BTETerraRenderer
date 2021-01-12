package com.mndk.mapdisp4bte.map;

import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public abstract class ExternalMapRenderer {

    public static final Map<String, ResourceLocation> resourceLocations = new HashMap<>();
    private static final List<Map.Entry<String, BufferedImage>> renderList
            = new ArrayList<>();

    private final RenderMapSource source;

    public ExternalMapRenderer(RenderMapSource source) {
        this.source = source;
    }



    public void initializeMapImageByPlayerCoordinate(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int level, RenderMapType type
    ) throws OutOfProjectionBoundsException {

        int[] tileCoord = this.playerPositionToTileCoord(playerX, playerZ, level);

        String tileID = genTileID(tileCoord[0]+tileDeltaX, tileCoord[1]+tileDeltaY, level, type, this.source);

        BufferedImage image = this.fetchMapSync(playerX, playerZ, tileDeltaX, tileDeltaY, level, type);

        renderList.add(new AbstractMap.SimpleEntry<>(tileID, image));
    }



    public abstract int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException;

    public abstract double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException;

    /**
     * This should return: [tileDeltaX, tileDeltaY, u, v]
     */
    protected abstract int[] getCornerMatrix(int i);



    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, level);

            String url = this.getUrlTemplate(tilePos[0] + tileDeltaX, tilePos[1] + tileDeltaY, level, type);

            return new URL(url).openConnection();
        }catch(OutOfProjectionBoundsException | IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public BufferedImage fetchMapSync(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {
        try {
            URLConnection connection = this.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, level, type);
            if(connection == null) return null;
            connection.connect();
            return ImageIO.read(connection.getInputStream());
        } catch(IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public abstract String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type);



    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int level, RenderMapType type,
            double y, float opacity,
            double px, double py, double pz,
            int tileDeltaX, int tileDeltaY
    ) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(px, pz, level);

            String tileID = genTileID(tilePos[0]+tileDeltaX, tilePos[1]+tileDeltaY, level, type, source);

            if(!renderList.isEmpty()) {
                // Cannot set all images to resource locations at once, because it would cause ConcurrentModificationException.
                // So instead, it's setting one image by frame.

                // TODO The code is messy, so re-categorize this

                Map.Entry<String, BufferedImage> entry = renderList.get(0);
                renderList.remove(0);
                DynamicTexture texture = new DynamicTexture(entry.getValue());
                ResourceLocation reloc =
                        Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(tileID, texture);
                resourceLocations.put(entry.getKey(), reloc);
            }

            if(!resourceLocations.containsKey(tileID)) {
                // If the tile is not loaded, load it in new thread
                resourceLocations.put(tileID, null);
                new Thread(() -> {
                    try {
                        initializeMapImageByPlayerCoordinate(px, pz, tileDeltaX, tileDeltaY, level, type);
                    } catch (OutOfProjectionBoundsException exception) {
                        exception.printStackTrace();
                    }
                }).start();
            }
            else if(resourceLocations.get(tileID) != null) {
                ResourceLocation resourceLocation = resourceLocations.get(tileID);

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
                            .color(1.f, 1.f, 1.f, opacity)
                            .endVertex();
                }

                t.draw();
            }

        } catch(OutOfProjectionBoundsException ignored) {}
    }



    public static String genTileID(int tileX, int tileY, int level, RenderMapType type, RenderMapSource source) {
        return "tilemap_" + source.getEnumName() + "_" + tileX + "_" + tileY + "_" + level + "_" + type.getEnumName();
    }

}
