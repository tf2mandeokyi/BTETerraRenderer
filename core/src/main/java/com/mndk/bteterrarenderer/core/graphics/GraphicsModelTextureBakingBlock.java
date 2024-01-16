package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelTextureBakingBlock<Key> extends SingleQueueBlock<Key, List<PreBakedModel>, List<GraphicsModel>> {

	@Override
	protected List<GraphicsModel> processInternal(Key key, @Nonnull List<PreBakedModel> preBakedModels) {
        List<GraphicsModel> models = new ArrayList<>(preBakedModels.size());
		for(PreBakedModel preBakedModel : preBakedModels) {
			NativeTextureWrapper textureObject = GlGraphicsManager.INSTANCE.allocateAndGetTextureObject(preBakedModel.getImage());
			models.add(new GraphicsModel(textureObject, preBakedModel.getQuads(), preBakedModel.getTriangles()));
		}
		return models;
	}
}
