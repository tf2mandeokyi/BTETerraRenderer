package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;

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
			NativeTextureWrapper textureObject = McConnector.client().glGraphicsManager.allocateAndGetTextureObject(preBakedModel.getImage());
			models.add(new GraphicsModel(textureObject, preBakedModel.getShapes()));
		}
		return models;
	}
}
