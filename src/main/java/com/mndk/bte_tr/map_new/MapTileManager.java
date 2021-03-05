package com.mndk.bte_tr.map_new;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapTileManager {

    public static MapTileManager instance = new MapTileManager();
    public static MapTileManager getInstance() { return instance; }

    private List<Map.Entry<String, BufferedImage>> imageRenderQueue;
    private final MapTileCache tileCache;

    private MapTileManager() {
        this.imageRenderQueue = new ArrayList<>();
        this.tileCache = new MapTileCache(1000 * 60, 1000);
    }

    public void addImageToRenderList(String tileId, BufferedImage image) {
        imageRenderQueue.add(new AbstractMap.SimpleEntry<>(tileId, image));
    }

    /**
     * This function must be called in the thread where the opengl context is found.
     */
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
                    tileCache.addTexture(tileKey, image);
                }
            } catch(Exception e) {
                e.printStackTrace();
                // Put the image data back to the queue if something went wrong
                newList.add(new AbstractMap.SimpleEntry<>(tileKey, image));
            }
        }

        imageRenderQueue = newList;

    }

    public MapTileCache getTileCache() {
        return this.tileCache;
    }

}
