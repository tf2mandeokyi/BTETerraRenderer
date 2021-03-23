package com.mndk.bteterrarenderer.map;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mndk.bteterrarenderer.map.bing.BingTileMap;
import com.mndk.bteterrarenderer.map.kakao_wtm.KakaoTileMap;
import com.mndk.bteterrarenderer.map.mercator.MercatorTileMap;
import com.mndk.bteterrarenderer.util.JsonUtil;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public abstract class ExternalTileMap {
	
	
	
	private static final int[][] CORNER_MATRIX = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };
	
	
	
	private static final int[][] CORNER_MATRIX_INVERT_LAT = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };
	
	
	
	static final int DEFAULT_MAX_THREAD = 2;
	static final int DEFAULT_ZOOM = 18;

	
	
	private final String id, name, tileUrl;
    private final Map<String, String> requestHeaders;
    private final ExecutorService downloadExecutor;
    protected final boolean invertLatitude, invertZoom;
    private final int defaultZoom;
    
    
    
    public static ExternalTileMap parse(JsonObject object) throws Exception {
    	String projectionId = JsonUtil.validateStringElement(object, "projection");
    	switch(projectionId.toLowerCase()) {
    		case "mercator": return new MercatorTileMap(object);
    		case "bing": return new BingTileMap(object);
    		case "kakao_wtm": return new KakaoTileMap(object);
    	}
    	throw new Exception(projectionId + " projection doesn't exist!");
    }
    
    
    
    protected ExternalTileMap(JsonObject object) throws Exception {
    	
    	this.id = JsonUtil.validateStringElement(object, "id");
    	this.name = JsonUtil.validateStringElement(object, "name");
    	this.tileUrl = JsonUtil.validateStringElement(object, "tile_url");
		this.defaultZoom = JsonUtil.validateIntegerElement(object, "default_zoom", DEFAULT_ZOOM);
		this.invertLatitude = JsonUtil.validateBooleanElement(object, "invert_lat", false);
		this.invertZoom = JsonUtil.validateBooleanElement(object, "invert_zoom", false);

		int maxThread = JsonUtil.validateIntegerElement(object, "max_thread", DEFAULT_MAX_THREAD);
		this.downloadExecutor = Executors.newFixedThreadPool(maxThread);

		this.requestHeaders = new HashMap<>();
		if(object.get("request_headers") != null) {
			JsonObject request_headers;
			try {
				request_headers = object.get("request_headers").getAsJsonObject();
			} catch(IllegalStateException e) { throw new Exception("request_headers should be an object!"); }
			
			for(Entry<String, JsonElement> entry : request_headers.entrySet()) {
				String key = entry.getKey();
				requestHeaders.put(key, JsonUtil.validateStringElement(request_headers, key));
			}
		}
    }



    public void initializeMapImageByPlayerCoordinate(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom) 
    		throws OutOfProjectionBoundsException {

        int[] tileCoord = this.playerPositionToTileCoord(playerX, playerZ, zoom);

        String tileId = this.genTileKey(tileCoord[0]+tileDeltaX, tileCoord[1]+tileDeltaY, zoom);

        BufferedImage image = this.fetchMapSync(playerX, playerZ, tileDeltaX, tileDeltaY, zoom);

        TileMapCache.getInstance().addImageToRenderQueue(tileId, image);
    }
    
    
    
    public abstract int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException;
    public abstract double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException;
    
    
    
    public String getUrlTemplate(int tileX, int tileY, int zoom) {
    	return replaceRandoms(tileUrl).replace("{z}", zoom + "").replace("{x}", tileX + "").replace("{y}", tileY + "");
    }
    
    
    
    private static String replaceRandoms(String url) {
    	Matcher m = Pattern.compile("\\{random:([^{}]+)\\}").matcher(url);
    	StringBuffer buffer = new StringBuffer();
    	Random r = new Random();
    	while(m.find()) {
    		String[] randoms = m.group(1).split(",");
    		m.appendReplacement(buffer, randoms[r.nextInt(randoms.length)]);
    	}
    	m.appendTail(buffer);
    	return buffer.toString();
    }



    /**
     * This should return: [tileDeltaX, tileDeltaY, u, v]
     */
    protected int[] getCornerMatrix(int i) {
    	return invertLatitude ? CORNER_MATRIX_INVERT_LAT[i] : CORNER_MATRIX[i];
    }
    
    
    
    protected int getZoomFromLevel(int level) {
    	return invertZoom ? defaultZoom + level : defaultZoom - level;
    }



    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, zoom);

            String url = this.getUrlTemplate(tilePos[0] + tileDeltaX, tilePos[1] + tileDeltaY, zoom);

            URLConnection connection = new URL(url).openConnection();
            
            for(Entry<String, String> entry : this.requestHeaders.entrySet()) {
            	connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setUseCaches(true);
            
            return connection;
            
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

            String tileKey = this.genTileKey(tilePos[0]+tileDeltaX, tilePos[1]+tileDeltaY, zoom);

            TileMapCache cache = TileMapCache.getInstance();

            cache.cacheAllImagesInQueue();

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
    
    
    
    public String getName() {
    	return name;
    }
    
    public String getId() {
    	return id;
    }



    public String genTileKey(int tileX, int tileY, int zoom) {
        return "tilemap_" + this.id + "_" + tileX + "_" + tileY + "_" + zoom;
    }
    
    
    
    @Override
    public String toString() {
    	return ExternalTileMap.class.getName() + "{id=" + id + ", name=" + name + ", tile_url=" + tileUrl + "}";
    }

}
