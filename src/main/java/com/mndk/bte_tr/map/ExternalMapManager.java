package com.mndk.bte_tr.map;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import com.mndk.bte_tr.map_new.MapTileCache;
import com.mndk.bte_tr.map_new.MapTileManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Deprecated
public abstract class ExternalMapManager {

    private final RenderMapSource source;
    private final ExecutorService downloadExecutor;

    public ExternalMapManager(RenderMapSource source, int maximumDownloadThreads) {
        this.source = source;
        this.downloadExecutor = Executors.newFixedThreadPool(maximumDownloadThreads);
    }



    public void initializeMapImageByPlayerCoordinate(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int zoom
    ) throws OutOfProjectionBoundsException {

        int[] tileCoord = this.playerPositionToTileCoord(playerX, playerZ, zoom);

        String tileId = genTileKey(tileCoord[0]+tileDeltaX, tileCoord[1]+tileDeltaY, zoom, this.source);

        BufferedImage image = this.fetchMapSync(playerX, playerZ, tileDeltaX, tileDeltaY, zoom);

        MapTileManager.getInstance().addImageToRenderList(tileId, image);
    }



    /**
     * This should return: [tileDeltaX, tileDeltaY, u, v]
     */
    protected abstract int[] getCornerMatrix(int i);
    public abstract int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException;
    public abstract double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException;
    protected abstract int getZoomFromLevel(int level);
    public abstract String getUrlTemplate(int tileX, int tileY, int level);



    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, zoom);

            String url = this.getUrlTemplate(tilePos[0] + tileDeltaX, tilePos[1] + tileDeltaY, zoom);

            return new URL(url).openConnection();
        }catch(OutOfProjectionBoundsException | IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public BufferedImage fetchMapSync(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom) {
        try {
            URLConnection connection = this.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, zoom);
            if(connection == null) return null;
            connection.connect();
            return ImageIO.read(connection.getInputStream());
        } catch(IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int level,
            double y, float opacity,
            double px, double py, double pz,
            int tileDeltaX, int tileDeltaY
    ) {
        try {
            int zoom = this.getZoomFromLevel(level);

            int[] tilePos = this.playerPositionToTileCoord(px, pz, zoom);

            String tileKey = genTileKey(tilePos[0]+tileDeltaX, tilePos[1]+tileDeltaY, zoom, source);

            MapTileCache cache = MapTileManager.getInstance().getTileCache();

            MapTileManager.getInstance().cacheAllImagesInQueue();

            if(!cache.isTileInDownloadingState(tileKey)) {
                if(!cache.textureExists(tileKey)) {
                    // If the tile is not loaded, load it in new thread
                    cache.setTileDownloadingState(tileKey, true);
                    this.downloadExecutor.execute(() -> {
                        try {
                            initializeMapImageByPlayerCoordinate(px, pz, tileDeltaX, tileDeltaY, zoom);
                            cache.setTileDownloadingState(tileKey, false);

                        } catch (OutOfProjectionBoundsException ignored) { }
                    });
                }
                else {

                    cache.bindTexture(tileKey);

                    // begin vertex
                    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

                    double[] temp;

                    // Convert boundaries
                    for (int i = 0; i < 4; i++) {

                        int[] mat = this.getCornerMatrix(i);
                        temp = tileCoordToPlayerPosition(tilePos[0] + mat[0] + tileDeltaX, tilePos[1] + mat[1] + tileDeltaY, zoom);

                        builder.pos(temp[0] - px, y - py, temp[1] - pz)
                                .tex(mat[2], mat[3])
                                .color(1.f, 1.f, 1.f, opacity)
                                .endVertex();
                    }

                    t.draw();
                }
            }

        } catch(OutOfProjectionBoundsException ignored) { }
    }



    public static String genTileKey(int tileX, int tileY, int zoom, RenderMapSource source) {
        return "tilemap_" + source + "_" + tileX + "_" + tileY + "_" + zoom;
    }

}
