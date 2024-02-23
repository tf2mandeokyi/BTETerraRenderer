package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class ImageBakingBlock<Key> extends SingleQueueBlock<Key, BufferedImage, NativeTextureWrapper> {

    @Override
    protected NativeTextureWrapper processInternal(Key key, @Nonnull BufferedImage image) {
        return McConnector.client().glGraphicsManager.allocateAndGetTextureObject(image);
    }
}
