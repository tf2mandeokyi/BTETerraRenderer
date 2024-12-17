package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.processor.block.SingleQueueBlock;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Model manager class that stores models & texture data.
 */
@Setter
public class GraphicsModelTextureBakingBlock<Key> extends SingleQueueBlock<Key, List<PreBakedModel>, List<GraphicsModel>> {

	private NativeTextureWrapper defaultTexture = null;

	@Override
	protected List<GraphicsModel> processInternal(Key key, @Nonnull List<PreBakedModel> preBakedModels) {
        List<GraphicsModel> models = new ArrayList<>(preBakedModels.size());
		for (PreBakedModel preBakedModel : preBakedModels) {
			BufferedImage image = preBakedModel.getImage();
			NativeTextureWrapper textureObject = image == null
					? defaultTexture
					: McConnector.client().glGraphicsManager.allocateAndGetTextureObject(image);
			models.add(new GraphicsModel(textureObject, preBakedModel.getShapes()));
		}
		return models;
	}
}
