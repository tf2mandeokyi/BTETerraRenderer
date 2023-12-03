package com.mndk.bteterrarenderer.core.graphics.baker;

import com.mndk.bteterrarenderer.core.util.processor.SimpleResourceCacheProcessor;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;

import java.awt.image.BufferedImage;
import java.net.URL;

public class URLBufferedImageBaker extends SimpleResourceCacheProcessor<URL, BufferedImage, Object> {

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
     * @param maximumSize        Maximum cache size. Set to -1 for no limits
     * @param debug              debug
     */
    public URLBufferedImageBaker(long expireMilliseconds, int maximumSize, boolean debug) {
        super(expireMilliseconds, maximumSize, debug);
    }

    @Override
    protected Object processResource(BufferedImage image) {
        return GlGraphicsManager.INSTANCE.allocateAndGetTextureObject(image);
    }

    @Override
    protected void deleteResource(Object allocatedObject) {
        GlGraphicsManager.INSTANCE.deleteTextureObject(allocatedObject);
    }
}
