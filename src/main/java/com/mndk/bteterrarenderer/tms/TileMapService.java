package com.mndk.bteterrarenderer.tms;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.projection.Projections;
import com.mndk.bteterrarenderer.storage.TileMapCache;
import com.mndk.bteterrarenderer.tms.proj.KakaoTileProjection;
import com.mndk.bteterrarenderer.tms.proj.TileProjection;
import com.mndk.bteterrarenderer.tms.proj.WebMercatorProjection;
import com.mndk.bteterrarenderer.tms.proj.WorldMercatorProjection;
import com.mndk.bteterrarenderer.tms.url.BingURLConverter;
import com.mndk.bteterrarenderer.tms.url.DefaultTileURLConverter;
import com.mndk.bteterrarenderer.tms.url.TileURLConverter;
import io.netty.buffer.ByteBufInputStream;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.http.Http;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileMapService {


    public static final int DEFAULT_ZOOM = 18;
    static final int DEFAULT_MAX_THREAD = 2;
    public static BufferedImage SOMETHING_WENT_WRONG;


    private final String id, name, urlTemplate;
    private final TileProjection tileProjection;
    private final TileURLConverter urlConverter;
    private final ExecutorService downloadExecutor;


    public static TileMapService parse(
            String fileName, String categoryName, String id, Map<String, Object> jsonObject
    ) throws Exception {

        String projectionId = (String) jsonObject.get("projection");
        id = fileName + "." + categoryName + "." + id;
        if(fileName != null && !"".equals(fileName) && !"default".equals(fileName)) {
            jsonObject.put("name", "[§7" + fileName + "§r] " + jsonObject.get("name"));
        }
        try {
            return new TileMapService(id, jsonObject);
        } catch(NullPointerException e) {
            throw new Exception(projectionId + " projection doesn't exist!");
        }
    }


    /**
     * @throws NullPointerException If the projection corresponding to its id does not exist
     */
    private TileMapService(String id, Map<String, Object> jsonObject) throws NullPointerException {

        this.id = id;
        this.name = (String) jsonObject.get("name");
        this.urlTemplate = (String) jsonObject.get("tile_url");

        String projectionId = (String) jsonObject.get("projection");
        this.tileProjection = Objects.requireNonNull(getTileProjection(projectionId));
        this.urlConverter = getTileURLConverter(projectionId);

        if(jsonObject.containsKey("default_zoom")) {
            int defaultZoom = (int) jsonObject.get("default_zoom");
            tileProjection.setDefaultZoom(defaultZoom);
            urlConverter.setDefaultZoom(defaultZoom);
        }
        if(jsonObject.containsKey("invert_zoom")) {
            boolean invertZoom = (boolean) jsonObject.get("invert_zoom");
            tileProjection.setInvertZoom(invertZoom);
            urlConverter.setInvertZoom(invertZoom);
        }
        if(jsonObject.containsKey("invert_lat"))
            tileProjection.setInvertLatitude((boolean) jsonObject.get("invert_lat"));


        int maxThread = (int) jsonObject.getOrDefault("max_thread", DEFAULT_MAX_THREAD);
        this.downloadExecutor = Executors.newFixedThreadPool(maxThread);
    }


    private static TileProjection getTileProjection(String projectionId) {
        switch(projectionId.toLowerCase()) {
            case "webmercator":
            case "mercator":
            case "bing": return new WebMercatorProjection();
            case "worldmercator": return new WorldMercatorProjection();
            case "kakao_wtm": return new KakaoTileProjection();
            default: return null;
        }
    }


    private static TileURLConverter getTileURLConverter(String projectionId) {
        if ("bing".equalsIgnoreCase(projectionId)) {
            return new BingURLConverter();
        }
        return new DefaultTileURLConverter();
    }


    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int relativeZoom,
            double y, float opacity,
            double playerX, double playerY, double playerZ,
            int tileDeltaX, int tileDeltaY
    ) {
        TileMapCache cache = TileMapCache.getInstance();
        int[] tileCoord;
        double[] geoCoord, gameCoord;

        try {
            geoCoord = Projections.getServerProjection().toGeo(playerX, playerZ);
            tileCoord = this.tileProjection.geoCoordToTileCoord(geoCoord[0], geoCoord[1], relativeZoom);
            int tileX = tileCoord[0] + tileDeltaX, tileY = tileCoord[1] + tileDeltaY;
            final String tileKey = this.genTileKey(tileX, tileY, relativeZoom);

            cache.cacheAllImagesInQueue();

            // Return if the requested tile is still in the downloading state
            if(cache.isTileInDownloadingState(tileKey)) return;

            if(!cache.textureExists(tileKey)) {
                // If the requested tile is not loaded, load it in the new thread and return
                String url = this.urlConverter.convertToUrl(this.urlTemplate, tileX, tileY, relativeZoom);
                this.downloadTile(tileKey, url);
                return;
            }

            cache.bindTexture(tileKey);
            // begin vertex
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            /*
             *  i=0 -------- i=1
             *   |            |
             *   |    TILE    |
             *   |            |
             *   |            |
             *  i=3 -------- i=2
             */
            for (int i = 0; i < 4; i++) {
                int[] mat = this.tileProjection.getCornerMatrix(i);
                geoCoord = tileProjection.tileCoordToGeoCoord(tileX + mat[0], tileY + mat[1], relativeZoom);
                gameCoord = Projections.getServerProjection().fromGeo(geoCoord[0], geoCoord[1]);

                builder.pos(gameCoord[0] - playerX, y - playerY, gameCoord[1] - playerZ)
                        .tex(mat[2], mat[3])
                        .color(1.f, 1.f, 1.f, opacity)
                        .endVertex();
            }
            t.draw();

        } catch(OutOfProjectionBoundsException ignored) {
        } catch(Exception e) {
            BTETerraRenderer.logger.warn("Caught an Exception while rendering tile images", e);
        }

    }


    private void downloadTile(String tileKey, String url) {
        TileMapCache cache = TileMapCache.getInstance();
        cache.setTileDownloadingState(tileKey, true);

        this.downloadExecutor.execute(() -> {
            BufferedImage image;
            try {
                ByteBufInputStream stream = new ByteBufInputStream(Http.get(url).get());
                image = ImageIO.read(stream);
            } catch(IOException | ExecutionException | InterruptedException e) {
                image = SOMETHING_WENT_WRONG;
            }
            TileMapCache.getInstance().addImageToRenderQueue(tileKey, image);
            cache.setTileDownloadingState(tileKey, false);
        });
    }


    public String genTileKey(int tileX, int tileY, int zoom) {
        return "tilemap_" + this.id + "_" + tileX + "_" + tileY + "_" + zoom;
    }


    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    @Override
    public String toString() {
        return TileMapService.class.getName() + "{id=" + id + ", name=" + name + ", tile_url=" + urlTemplate + "}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileMapService that = (TileMapService) o;
        return id.equals(that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    static {
        try {
            SOMETHING_WENT_WRONG = ImageIO.read(
                    Objects.requireNonNull(TileMapService.class.getClassLoader().getResourceAsStream(
                            "assets/" + BTETerraRenderer.MODID + "/textures/internal_error_image.png"
                    ))
            );

            // Converting the same image to resource every time might cause a performance issue.
            // TODO solve this issue
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
