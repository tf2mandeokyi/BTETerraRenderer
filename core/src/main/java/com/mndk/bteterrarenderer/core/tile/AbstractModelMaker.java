package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.util.processor.CacheableProcessorModel;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.GraphicsModel;

import java.util.List;

public abstract class AbstractModelMaker<Key> extends CacheableProcessorModel<Key, Key, List<GraphicsModel>> {

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
     * @param maximumSize        Maximum cache size. Set to -1 for no limits
     * @param debug              debug
     */
    protected AbstractModelMaker(long expireMilliseconds, int maximumSize, boolean debug) {
        super(expireMilliseconds, maximumSize, debug);
    }

    @Override
    protected void deleteResource(List<GraphicsModel> graphicsModels) {
        for(GraphicsModel model : graphicsModels) {
            GlGraphicsManager.INSTANCE.deleteTextureObject(model.getTextureObject());
        }
    }
}
