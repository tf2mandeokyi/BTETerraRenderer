package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class TextureManager {

    private final Map<String, Integer> textureIdMap = new HashMap<>();

    protected abstract NativeTextureWrapper getMissingTextureObject();
    public final NativeTextureWrapper allocateAndGetTextureObject(String modId, @Nullable BufferedImage image) {
        if (image == null) return this.getMissingTextureObject();
        int id = textureIdMap.getOrDefault(modId, 0);
        textureIdMap.put(modId, id + 1);
        return this.allocateAndGetTextureObject(modId, id, image);
    }
    protected abstract NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, @Nonnull BufferedImage image);
    protected abstract void deleteTextureObjectInternal(NativeTextureWrapper textureObject);
    public final void deleteTextureObject(NativeTextureWrapper textureObject) {
        if (textureObject.isDeleted()) return;
        deleteTextureObjectInternal(textureObject);
        textureObject.markAsDeleted();
    }

}
