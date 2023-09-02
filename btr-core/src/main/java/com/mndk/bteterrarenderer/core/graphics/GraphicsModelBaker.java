package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.BtrUtil;
import com.mndk.bteterrarenderer.core.util.queue.QueueNodeProcessor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelBaker<Key> extends QueueNodeProcessor<GraphicsModelBaker.ModelBakeRequest<Key>> {

	private static final GraphicsModelBaker<?> INSTANCE =
			new GraphicsModelBaker<>(1000 * 60 * 20 /* 20 minutes */, 10000);

	public static <ID> GraphicsModelBaker<ID> getInstance() {
		return BtrUtil.uncheckedCast(INSTANCE);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	private static final boolean DEBUG = false;
	private static void log(String message) {
		if(DEBUG) LOGGER.info(message);
	}

	private static final int BAKE_AT_A_TIME = 5;

	private final int maximumSize;
	private final Map<Key, GraphicsModelWrapper> bakedModelMap = new HashMap<>();
	private final Map<Key, ModelBakingState> bakingStateMap = new HashMap<>();
	private final long expireMilliseconds;

	private GraphicsModelBaker(long expireMilliseconds, int maximumSize) {
		super(700 /* 0.7 seconds */);
		this.expireMilliseconds = expireMilliseconds;
		this.maximumSize = maximumSize;
	}

	private synchronized GraphicsModel validateAndGetModel(Key modelKey) {
		if (!bakedModelMap.containsKey(modelKey)) throw new NullPointerException();
		return bakedModelMap.get(modelKey).model;
	}

	public synchronized ModelBakingState getModelBakingState(Key modelKey) {
		if(bakedModelMap.containsKey(modelKey)) {
			return ModelBakingState.BAKED;
		}
		ModelBakingState state = bakingStateMap.get(modelKey);
		return state != null ? state : ModelBakingState.NOT_BAKED;
	}

	public synchronized void setModelInPreparingState(Key modelKey) {
		bakingStateMap.put(modelKey, ModelBakingState.PREPARING);
	}

	public synchronized void modelPreparingError(Key modelKey) {
		bakingStateMap.put(modelKey, ModelBakingState.ERROR);
	}

	public synchronized void modelBakingReady(Key modelKey, PreBakedModel preBakedModel) {
		bakingStateMap.put(modelKey, ModelBakingState.BAKING);
		this.offerQueue(new ModelBakeRequest<>(modelKey, preBakedModel));
	}

	/** Must be called on a render thread */
	private synchronized void bakeModel(Key modelKey, PreBakedModel preBakedModel) {
		if(this.maximumSize != -1 && bakedModelMap.size() >= this.maximumSize) {
			this.deleteOldestModel();
		}

		int glId = GraphicsModelVisualManager.allocateAndUploadTexture(preBakedModel.getImage());
		GraphicsModel model = new GraphicsModel(glId, preBakedModel.getQuads());

		bakingStateMap.remove(modelKey);
		bakedModelMap.put(modelKey, new GraphicsModelWrapper(model, System.currentTimeMillis()));
		log("Added texture " + modelKey + " (Size: " + bakedModelMap.size() + ")");
	}

	public synchronized GraphicsModel updateAndGetModel(Key modelKey) {
		GraphicsModel model = validateAndGetModel(modelKey);
		this.bakedModelMap.get(modelKey).lastUpdated = System.currentTimeMillis();
		return model;
	}

	private synchronized void deleteModel(Key modelKey) {
		if (bakedModelMap.containsKey(modelKey)) {
			GraphicsModel model = bakedModelMap.get(modelKey).model;
			bakedModelMap.remove(modelKey);
			GraphicsModelVisualManager.glDeleteTexture(model.getTextureGlId());
			log("Deleted texture " + modelKey + " (Size: " + bakedModelMap.size() + ")");
		}
	}

	private synchronized void deleteOldestModel() {
		Key oldestKey = null; long oldest = Long.MAX_VALUE;
		for (Map.Entry<Key, GraphicsModelWrapper> entry : bakedModelMap.entrySet()) {
			GraphicsModelWrapper wrapper = entry.getValue();
			if(wrapper.lastUpdated < oldest) {
				oldestKey = entry.getKey();
				oldest = wrapper.lastUpdated;
			}
		}
		if(oldestKey != null) {
			this.deleteModel(oldestKey);
		}
	}

	public synchronized void cleanup() {
		long now = System.currentTimeMillis();
		ArrayList<Key> deleteList = new ArrayList<>();
		synchronized (this) {
			for (Map.Entry<Key, GraphicsModelWrapper> entry : bakedModelMap.entrySet()) {
				GraphicsModelWrapper wrapper = entry.getValue();
				if(wrapper.lastUpdated + this.expireMilliseconds > now) continue;
				deleteList.add(entry.getKey());
			}
		}
		if(!deleteList.isEmpty()) {
			log("Cleaning up...");
			for (Key modelKey : deleteList) {
				this.deleteModel(modelKey);
			}
		}
		this.updateQueueNode();
	}

	/** Must be called on a render thread */
	protected synchronized void processQueue(Queue<ModelBakeRequest<Key>> queue) {
		List<ModelBakeRequest<Key>> errors = new ArrayList<>();

		for (int i = 0; i < BAKE_AT_A_TIME && !queue.isEmpty(); i++) {
			ModelBakeRequest<Key> element = queue.poll();
			if (element == null) continue;
			if (element.preBakedModel.getImage() == null) continue;

			try {
				bakeModel(element.key, element.preBakedModel);
			} catch (Exception e) {
				LOGGER.error("Error while processing queue", e);
				// Put the image data back to the queue if something went wrong
				errors.add(element);
			}
		}
		queue.addAll(errors);
	}

	@RequiredArgsConstructor
	public static class ModelBakeRequest<Key> {
		private final Key key;
		private final PreBakedModel preBakedModel;
	}

	@AllArgsConstructor
	private static class GraphicsModelWrapper {
		private final GraphicsModel model;
		private long lastUpdated;
	}
}
