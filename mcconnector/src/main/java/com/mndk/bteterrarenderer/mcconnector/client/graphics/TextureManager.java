package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class TextureManager {

    private final Map<String, Integer> textureIdMap = new HashMap<>();

    public abstract NativeTextureWrapper getMissingTextureObject();
    public final NativeTextureWrapper allocateAndGetTextureObject(String modId, BufferedImage image) {
        int id = textureIdMap.getOrDefault(modId, 0);
        textureIdMap.put(modId, id + 1);
        return this.allocateAndGetTextureObject(modId, id, image);
    }
    protected abstract NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image);
    protected abstract void deleteTextureObjectInternal(NativeTextureWrapper textureObject);
    public final void deleteTextureObject(NativeTextureWrapper textureObject) {
        if (textureObject.isDeleted()) return;
        deleteTextureObjectInternal(textureObject);
        textureObject.markAsDeleted();
    }

}
