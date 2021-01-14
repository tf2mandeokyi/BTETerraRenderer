package com.mndk.mapdisp4bte.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapTileManager {

    public static MapTileManager instance = new MapTileManager();
    public static MapTileManager getInstance() { return instance; }

    private Map<String, ResourceLocation> resourceLocations;
    private final Set<String> preservedIdSet;
    private List<Map.Entry<String, BufferedImage>> renderList;

    private MapTileManager() {
        this.resourceLocations = new HashMap<>();
        this.preservedIdSet = new HashSet<>();
        this.renderList = new ArrayList<>();
    }

    public void addImageToRenderList(String tileId, BufferedImage image) {
        renderList.add(new AbstractMap.SimpleEntry<>(tileId, image));
    }

    /**
     * Note: this function must be called in the thread where the opengl context is found.
     */
    public void convertAllImagesToResoureLocations() {
        List<Map.Entry<String, BufferedImage>> newList = new ArrayList<>();

        while(!renderList.isEmpty()) {
            Map.Entry<String, BufferedImage> entry = renderList.get(0);
            renderList.remove(0);

            if(entry != null) {
                String tileId = entry.getKey();
                BufferedImage image = entry.getValue();

                try {
                    if (entry.getValue() != null) {
                        DynamicTexture texture = new DynamicTexture(entry.getValue());
                        ResourceLocation textureLocation =
                                Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(tileId, texture);
                        resourceLocations.put(tileId, textureLocation);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    newList.add(new AbstractMap.SimpleEntry<>(tileId, image)); // Try again if the error happens
                }
            }
        }

        renderList = newList;

    }

    public void setTileIdOnly(String tileId) {
        resourceLocations.put(tileId, null);
    }

    public boolean tileIdExists(String tileId) {
        return resourceLocations.containsKey(tileId);
    }

    public ResourceLocation getResourceLocationByTileId(String tileId) {
        return resourceLocations.get(tileId);
    }

    public void addTileIdToPreserveList(String tileId) {
        preservedIdSet.add(tileId);
    }

    /*
     * I'm not sure whether this is working well
     *
     * TODO figure out whether this is working well
     */
    public void freeUnusedResourceLocations() {
        Map<String, ResourceLocation> newMap = new HashMap<>();
        Set<String> tileIdSet = resourceLocations.keySet();
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();

        for(String tileId : tileIdSet) {
            if(preservedIdSet.contains(tileId)) {
                newMap.put(tileId, resourceLocations.get(tileId));
            }
            else {
                manager.deleteTexture(resourceLocations.get(tileId));
            }
        }
        resourceLocations = newMap;
        preservedIdSet.clear();
    }

}
