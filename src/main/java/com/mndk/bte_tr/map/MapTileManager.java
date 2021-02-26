package com.mndk.bte_tr.map;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapTileManager {

    public static MapTileManager instance = new MapTileManager();
    public static MapTileManager getInstance() { return instance; }

    private List<Map.Entry<String, BufferedImage>> renderList;
    private final MapTileCache tileCache;

    private MapTileManager() {
        this.renderList = new ArrayList<>();
        this.tileCache = new MapTileCache(1000 * 60 * 5, 100);
    }

    public void addImageToRenderList(String tileId, BufferedImage image) {
        renderList.add(new AbstractMap.SimpleEntry<>(tileId, image));
    }

    /**
     * Note: this function must be called in the thread where the opengl context is found.
     */
    public void cacheAllImages() {
        List<Map.Entry<String, BufferedImage>> newList = new ArrayList<>();

        while(!renderList.isEmpty()) {
            Map.Entry<String, BufferedImage> entry = renderList.get(0);
            renderList.remove(0);

            if(entry != null) {
                String tileKey = entry.getKey();
                BufferedImage image = entry.getValue();

                try {
                    if (entry.getValue() != null) {
                        tileCache.addTexture(tileKey, image);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    newList.add(new AbstractMap.SimpleEntry<>(tileKey, image)); // Try again if the error happens
                }
            }
        }

        renderList = newList;

    }

    public MapTileCache getTileCache() {
        return this.tileCache;
    }

}
