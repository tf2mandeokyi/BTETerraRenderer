package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.queue.QueueNodeProcessor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelBaker extends QueueNodeProcessor<GraphicsModelBaker.TextureBakeRequest> {

	public static final GraphicsModelBaker INSTANCE =
			new GraphicsModelBaker(1000 * 60 * 5 /* 5 minutes */, 10000);

	private static final Logger LOGGER = LogManager.getLogger();
	private static final boolean DEBUG = false;
	private static void log(String message) {
		if(DEBUG) LOGGER.info(message);
	}

	private static final int BAKE_AT_A_TIME = 5;

	private final int maximumSize;
	private final Map<Object, GraphicsModelWrapper> registeredModelMap = new HashMap<>();
	private final Set<Object> downloadingKeys = new HashSet<>();
	private final Set<Object> bakingKeys = new HashSet<>();
	private final Set<Object> errorKeys = new HashSet<>();
	private final long expireMilliseconds;

	public GraphicsModelBaker(long expireMilliseconds, int maximumSize) {
		super(700 /* 0.7 seconds */);
		this.expireMilliseconds = expireMilliseconds;
		this.maximumSize = maximumSize;
	}

	private synchronized GraphicsModel validateAndGetModel(Object modelKey) {
		if (!registeredModelMap.containsKey(modelKey)) throw new NullPointerException();
		return registeredModelMap.get(modelKey).model;
	}

	public synchronized void setTextureInDownloadingState(Object modelKey) {
		downloadingKeys.add(modelKey);
	}

	public synchronized void textureDownloadingError(Object modelKey) {
		downloadingKeys.remove(modelKey);
		errorKeys.add(modelKey);
	}

	public synchronized void textureDownloadingComplete(Object modelKey, BufferedImage image,
														List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads) {
		downloadingKeys.remove(modelKey);
		bakingKeys.add(modelKey);
		this.offerQueue(new TextureBakeRequest(modelKey, image, quads));
	}

	public synchronized boolean isTextureNotReady(Object modelKey) {
		return downloadingKeys.contains(modelKey) || bakingKeys.contains(modelKey);
	}

	public synchronized boolean isTextureError(Object modelKey) {
		return errorKeys.contains(modelKey);
	}

	/** Must be called on a render thread */
	private synchronized void bakeModel(Object modelKey, BufferedImage image,
									   List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads) {
		if(this.maximumSize != -1 && registeredModelMap.size() >= this.maximumSize) {
			this.deleteOldestModel();
		}
		int glId = ModelGraphicsManager.allocateAndUploadTexture(image);
		this.registerModel(modelKey, new GraphicsModel(glId, quads));
	}

	private synchronized void registerModel(Object modelKey, GraphicsModel model) {
		bakingKeys.remove(modelKey);
		registeredModelMap.put(modelKey, new GraphicsModelWrapper(model, System.currentTimeMillis()));
		log("Added texture " + modelKey + " (Size: " + registeredModelMap.size() + ")");
	}

	public synchronized boolean modelExists(Object modelKey) {
		return registeredModelMap.containsKey(modelKey);
	}

	public synchronized GraphicsModel updateAndGetModel(Object modelKey) {
		GraphicsModel model = validateAndGetModel(modelKey);
		this.registeredModelMap.get(modelKey).lastUpdated = System.currentTimeMillis();
		return model;
	}

	private synchronized void deleteModel(Object modelKey) {
		if (registeredModelMap.containsKey(modelKey)) {
			GraphicsModel model = registeredModelMap.get(modelKey).model;
			registeredModelMap.remove(modelKey);
			ModelGraphicsManager.glDeleteTexture(model.getTextureGlId());
			log("Deleted texture " + modelKey + " (Size: " + registeredModelMap.size() + ")");
		}
	}

	private void deleteOldestModel() {
		Object oldestKey = null; long oldest = Long.MAX_VALUE;
		for (Map.Entry<Object, GraphicsModelWrapper> entry : registeredModelMap.entrySet()) {
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
		ArrayList<Object> deleteList = new ArrayList<>();
		synchronized (this) {
			for (Map.Entry<Object, GraphicsModelWrapper> entry : registeredModelMap.entrySet()) {
				GraphicsModelWrapper wrapper = entry.getValue();
				if(wrapper.lastUpdated + this.expireMilliseconds > now) continue;
				deleteList.add(entry.getKey());
			}
		}
		if(!deleteList.isEmpty()) {
			log("Cleaning up...");
			for (Object modelKey : deleteList) {
				this.deleteModel(modelKey);
			}
		}
		this.updateQueueNode();
	}

	/** Must be called on a render thread */
	protected synchronized void processQueue(Queue<TextureBakeRequest> queue) {
		List<TextureBakeRequest> errors = new ArrayList<>();

		for (int i = 0; i < BAKE_AT_A_TIME && !queue.isEmpty(); i++) {
			TextureBakeRequest element = queue.poll();
			if (element == null) continue;

			BufferedImage image = element.image;
			if (image == null) continue;

			try {
				bakeModel(element.key, image, element.quads);
			} catch (Exception e) {
				e.printStackTrace();
				// Put the image data back to the queue if something went wrong
				errors.add(element);
			}
		}
		queue.addAll(errors);
	}

	@RequiredArgsConstructor
	public static class TextureBakeRequest {
		private final Object key;
		private final BufferedImage image;
		private final List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads;
	}

	@AllArgsConstructor
	private static class GraphicsModelWrapper {
		private final GraphicsModel model;
		private long lastUpdated;
	}
}
