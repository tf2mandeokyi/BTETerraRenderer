package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class ImageBakingBlock<Key> extends SingleQueueBlock<Key, BufferedImage, NativeTextureWrapper> {

    @Override
    protected NativeTextureWrapper processInternal(Key key, @Nonnull BufferedImage image) {
        return GlGraphicsManager.INSTANCE.allocateAndGetTextureObject(image);
    }
}
