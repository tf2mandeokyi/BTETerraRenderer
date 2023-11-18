package com.mndk.bteterrarenderer.core.graphics.baker;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModelVisualManager;
import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.model.PreBakedModel;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.processor.SimpleResourceCacheProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelTextureBaker<Key> extends SimpleResourceCacheProcessor<Key, List<PreBakedModel>, List<GraphicsModel>> {

	private static final GraphicsModelTextureBaker<?> INSTANCE =
			new GraphicsModelTextureBaker<>(1000 * 60 * 20 /* 20 minutes */, 10000, false);
	public static <ID> GraphicsModelTextureBaker<ID> getInstance() {
		return BTRUtil.uncheckedCast(INSTANCE);
	}

	private GraphicsModelTextureBaker(long expireMilliseconds, int maximumSize, boolean debug) {
		super(expireMilliseconds, maximumSize, debug);
	}

	@Override
	protected List<GraphicsModel> processResource(List<PreBakedModel> preBakedModels) {
		List<GraphicsModel> models = new ArrayList<>(preBakedModels.size());
		for(PreBakedModel preBakedModel : preBakedModels) {
			Object textureObject = GraphicsModelVisualManager.allocateAndGetTextureObject(preBakedModel.getImage());
			models.add(new GraphicsModel(textureObject, preBakedModel.getShapes()));
		}
		return models;
	}

	@Override
	protected void deleteResource(List<GraphicsModel> graphicsModels) {
		for(GraphicsModel model : graphicsModels) {
			GraphicsModelVisualManager.deleteTextureObject(model.getTextureObject());
		}
	}
}
