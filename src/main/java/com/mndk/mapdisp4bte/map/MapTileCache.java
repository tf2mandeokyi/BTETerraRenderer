package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.MapDisplayer4BTE;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapTileCache {



    public static final MapTileCache instance = new MapTileCache(1000 * 60 * 5); // 5-minutes-span cache
    private static boolean debug = false;
    private static void log(String message) {
        if(debug) MapDisplayer4BTE.logger.info(message);
    }



    private final Map<String, GLIdWithDownloadDateWrapper> glTextureIdMap;
    private final Set<String> downloadingTileKeys;
    private final long expireMilliseconds;



    private MapTileCache(long expireMilliseconds) {
        this.glTextureIdMap = new HashMap<>();
        this.downloadingTileKeys = new HashSet<>();
        this.expireMilliseconds = expireMilliseconds;
    }



    private int validateAndGetGlId(String tileKey) {
        synchronized (glTextureIdMap) {
            if (!glTextureIdMap.containsKey(tileKey)) throw new NullPointerException();
            return glTextureIdMap.get(tileKey).glId;
        }
    }



    public void setTileDownloadingState(String tileKey, boolean state) {
        synchronized (downloadingTileKeys) {
            if(state) {
                downloadingTileKeys.add(tileKey);
            } else {
                downloadingTileKeys.remove(tileKey);
            }
        }
    }



    public boolean isTileInDownloadingState(String tileKey) {
        synchronized (downloadingTileKeys) {
            return downloadingTileKeys.contains(tileKey);
        }
    }



    public void addTexture(String tileKey, BufferedImage image) {
        synchronized (this.glTextureIdMap) {
            int glId = initializeTile(image);
            glTextureIdMap.put(tileKey, new GLIdWithDownloadDateWrapper(glId, System.currentTimeMillis()));
            log("Added texture " + tileKey + " (Size: " + glTextureIdMap.size() + ")");
        }
    }



    public boolean textureExists(String tileKey) {
        synchronized (this.glTextureIdMap) {
            return glTextureIdMap.containsKey(tileKey);
        }
    }



    public void bindTexture(String tileKey) {
        synchronized (this.glTextureIdMap) {
            int glId = validateAndGetGlId(tileKey);
            this.glTextureIdMap.get(tileKey).date = System.currentTimeMillis();
            GlStateManager.bindTexture(glId);
        }
    }



    private void deleteTexture(String tileKey) {
        synchronized (this.glTextureIdMap) {
            if (glTextureIdMap.containsKey(tileKey)) {
                int glId = glTextureIdMap.get(tileKey).glId;
                glTextureIdMap.remove(tileKey);
                GlStateManager.deleteTexture(glId);
                log("Deleted texture " + tileKey);
            }
        }
    }



    public void cleanup() {
        long now = System.currentTimeMillis();
        ArrayList<String> deleteList = new ArrayList<>();
        synchronized (this.glTextureIdMap) {
            for (Map.Entry<String, GLIdWithDownloadDateWrapper> entry : glTextureIdMap.entrySet()) {
                GLIdWithDownloadDateWrapper wrapper = entry.getValue();
                if(wrapper.date + this.expireMilliseconds < now) {
                    deleteList.add(entry.getKey());
                }
            }
        }
        if(!deleteList.isEmpty()) log("Cleaning up...");
        for(String key : deleteList) {
            this.deleteTexture(key);
        }
    }



    private static int initializeTile(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int glTextureId = TextureUtil.glGenTextures();

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);

        TextureUtil.allocateTexture(glTextureId, width, height);
        TextureUtil.uploadTexture(glTextureId, imageData, width, height);

        return glTextureId;
    }



    private static class GLIdWithDownloadDateWrapper {
        final int glId; long date;
        public GLIdWithDownloadDateWrapper(int glId, long date) {
            this.glId = glId; this.date = date;
        }
    }
}
