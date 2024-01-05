package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;

import java.awt.image.BufferedImage;

public class ImageBakingBlock<Key> extends SingleQueueBlock<Key, BufferedImage, Object> {

    @Override
    protected Object processInternal(Key key, BufferedImage image) {
        return GlGraphicsManager.INSTANCE.allocateAndGetTextureObject(image);
    }
}
