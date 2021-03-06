package com.mndk.bte_tr.map;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;

import java.awt.image.BufferedImage;
import java.util.*;

import com.mndk.bte_tr.BTETerraRenderer;

public class TileMapCache {
	
	
	
    public static TileMapCache instance = new TileMapCache(1000 * 60, 1000);
    public static TileMapCache getInstance() { return instance; }



    private static final boolean DEBUG = false;
    private static void log(String message) {
        if(DEBUG) BTETerraRenderer.logger.debug(message);
    }



    private final int maximumSize;
    private final Map<String, GLIdWrapper> glTextureIdMap;
    private final Set<String> downloadingTileKeys;
    private final long expireMilliseconds;
    private List<Map.Entry<String, BufferedImage>> imageRenderQueue;



    public TileMapCache(long expireMilliseconds, int maximumSize) {
        this.glTextureIdMap = new HashMap<>();
        this.downloadingTileKeys = new HashSet<>();
        this.expireMilliseconds = expireMilliseconds;
        this.maximumSize = maximumSize;
        this.imageRenderQueue = new ArrayList<>();
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
                this.deleteTheOldestData();
            }
            int glId = initializeTile(image);
            glTextureIdMap.put(tileKey, new GLIdWrapper(glId, System.currentTimeMillis()));
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
            this.glTextureIdMap.get(tileKey).lastUpdated = System.currentTimeMillis();
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



    private void deleteTheOldestData() {
        String oldestKey = null; long oldest = Long.MAX_VALUE;
        for (Map.Entry<String, GLIdWrapper> entry : glTextureIdMap.entrySet()) {
            GLIdWrapper wrapper = entry.getValue();
            if(wrapper.lastUpdated < oldest) {
                oldestKey = entry.getKey();
                oldest = wrapper.lastUpdated;
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
            for (Map.Entry<String, GLIdWrapper> entry : glTextureIdMap.entrySet()) {
                GLIdWrapper wrapper = entry.getValue();
                if(wrapper.lastUpdated + this.expireMilliseconds < now) {
                    deleteList.add(entry.getKey());
                }
            }
        }
        if(!deleteList.isEmpty()) log("Cleaning up...");
        for(String key : deleteList) {
            this.deleteTexture(key);
        }
    }



    /**
     * @return The GL texture id associated with the image texture.
     * */
    private static int initializeTile(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int glTextureId = TextureUtil.glGenTextures();

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);

        TextureUtil.allocateTexture(glTextureId, width, height);
        TextureUtil.uploadTexture(glTextureId, imageData, width, height);

        return glTextureId;
    }
    
    
    
    public void addImageToRenderList(String tileId, BufferedImage image) {
        imageRenderQueue.add(new AbstractMap.SimpleEntry<>(tileId, image));
    }
    
    
    
    public void cacheAllImagesInQueue() {
        List<Map.Entry<String, BufferedImage>> newList = new ArrayList<>();

        while(!imageRenderQueue.isEmpty()) {
        	// To prevent ConcurrentModificationException, the code is caching one image at a time.
            Map.Entry<String, BufferedImage> entry = imageRenderQueue.get(0);
            imageRenderQueue.remove(0);
            if(entry == null) continue;
            
            String tileKey = entry.getKey();
            BufferedImage image = entry.getValue();

            try {
                if (entry.getValue() != null) {
                    this.addTexture(tileKey, image);
                }
            } catch(Exception e) {
                e.printStackTrace();
                // Put the image data back to the queue if something went wrong
                newList.add(new AbstractMap.SimpleEntry<>(tileKey, image));
            }
        }

        imageRenderQueue = newList;

    }



    private static class GLIdWrapper {
        final int glId; long lastUpdated;
        public GLIdWrapper(int glId, long date) {
            this.glId = glId; this.lastUpdated = date;
        }
    }
}
