package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.processor.SimpleResourceCacheProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelBaker<Key> extends SimpleResourceCacheProcessor<Key, List<PreBakedModel>, List<GraphicsModel>> {

	private static final GraphicsModelBaker<?> INSTANCE =
			new GraphicsModelBaker<>(1000 * 60 * 20 /* 20 minutes */, 10000, false);
	public static <ID> GraphicsModelBaker<ID> getInstance() {
		return BtrUtil.uncheckedCast(INSTANCE);
	}

	private GraphicsModelBaker(long expireMilliseconds, int maximumSize, boolean debug) {
		super(expireMilliseconds, maximumSize, debug);
	}

	@Override
	protected List<GraphicsModel> processResource(List<PreBakedModel> preBakedModels) {
		List<GraphicsModel> models = new ArrayList<>(preBakedModels.size());
		for(PreBakedModel preBakedModel : preBakedModels) {
			int glId = GraphicsModelVisualManager.allocateAndUploadTexture(preBakedModel.getImage());
			models.add(new GraphicsModel(glId, preBakedModel.getQuads()));
		}
		return models;
	}

	@Override
	protected void deleteResource(List<GraphicsModel> graphicsModels) {
		for(GraphicsModel model : graphicsModels) {
			GraphicsModelVisualManager.glDeleteTexture(model.getTextureGlId());
		}
	}
}
