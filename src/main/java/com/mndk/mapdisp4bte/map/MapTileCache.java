package com.mndk.mapdisp4bte.map;

import com.mndk.mapdisp4bte.MapDisplayer4BTE;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapTileCache {



    private static final boolean DEBUG = false;
    private static void log(String message) {
        if(DEBUG) MapDisplayer4BTE.logger.info(message);
    }



    private final int maximumSize;
    private final Map<String, GLIdWithDownloadDateWrapper> glTextureIdMap;
    private final Set<String> downloadingTileKeys;
    private final long expireMilliseconds;



    public MapTileCache(long expireMilliseconds, int maximumSize) {
        this.glTextureIdMap = new HashMap<>();
        this.downloadingTileKeys = new HashSet<>();
        this.expireMilliseconds = expireMilliseconds;
        this.maximumSize = maximumSize;
    }



    private int validateAndGetGlId(String tileKey) {
        synchronized (this) {
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
        synchronized (this) {
            if(this.maximumSize != -1 && glTextureIdMap.size() >= this.maximumSize) {
                this.deleteTheOldestOne();
            }
            int glId = initializeTile(image);
            glTextureIdMap.put(tileKey, new GLIdWithDownloadDateWrapper(glId, System.currentTimeMillis()));
            log("Added texture " + tileKey + " (Size: " + glTextureIdMap.size() + ")");
        }
    }



    public boolean textureExists(String tileKey) {
        synchronized (this) {
            return glTextureIdMap.containsKey(tileKey);
        }
    }



    public void bindTexture(String tileKey) {
        synchronized (this) {
            int glId = validateAndGetGlId(tileKey);
            this.glTextureIdMap.get(tileKey).date = System.currentTimeMillis();
            GlStateManager.bindTexture(glId);
        }
    }



    private void deleteTexture(String tileKey) {
        synchronized (this) {
            if (glTextureIdMap.containsKey(tileKey)) {
                int glId = glTextureIdMap.get(tileKey).glId;
                glTextureIdMap.remove(tileKey);
                GlStateManager.deleteTexture(glId);
                log("Deleted texture " + tileKey);
            }
        }
    }



    private void deleteTheOldestOne() {
        String oldestKey = null; long oldest = Long.MAX_VALUE;
        for (Map.Entry<String, GLIdWithDownloadDateWrapper> entry : glTextureIdMap.entrySet()) {
            GLIdWithDownloadDateWrapper wrapper = entry.getValue();
            if(wrapper.date < oldest) {
                oldestKey = entry.getKey();
                oldest = wrapper.date;
            }
        }
        if(oldestKey != null) {
            this.deleteTexture(oldestKey);
        }
    }



    public void cleanup() {
        long now = System.currentTimeMillis();
        ArrayList<String> deleteList = new ArrayList<>();
        synchronized (this) {
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
