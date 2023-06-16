package com.mndk.bteterrarenderer.graphics;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.tile.ModelGraphicsConnector;
import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelManager {

	private static final GraphicsModelManager INSTANCE = new GraphicsModelManager(1000 * 60 * 5, 10000);
	public static GraphicsModelManager getInstance() { return INSTANCE; }

	private static final boolean DEBUG = false;
	private static void log(String message) {
		if(DEBUG) BTETerraRendererConstants.LOGGER.debug(message);
	}

	private static final int REGISTER_AT_A_TIME = 5;

	private final int maximumSize;
	private final Map<String, GraphicsModelWrapper> registeredModelMap;
	private final Set<String> downloadingTextureKeys;
	private final long expireMilliseconds;
	private Queue<TextureRenderQueueElement> imageRenderQueue;

	public GraphicsModelManager(long expireMilliseconds, int maximumSize) {
		this.registeredModelMap = new HashMap<>();
		this.downloadingTextureKeys = new HashSet<>();
		this.expireMilliseconds = expireMilliseconds;
		this.maximumSize = maximumSize;
		this.imageRenderQueue = new ArrayDeque<>();
	}

	private synchronized GraphicsModel validateAndGetModel(String modelKey) {
		if (!registeredModelMap.containsKey(modelKey)) throw new NullPointerException();
		return registeredModelMap.get(modelKey).model;
	}

	public synchronized void setTextureInDownloadingState(String modelKey) {
		downloadingTextureKeys.add(modelKey);
	}

	public synchronized void textureDownloadingComplete(String modelKey, BufferedImage image,
														List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads) {
		downloadingTextureKeys.remove(modelKey);
		imageRenderQueue.offer(new TextureRenderQueueElement(modelKey, image, quads));
	}

	public synchronized boolean isTextureInDownloadingState(String modelKey) {
		return downloadingTextureKeys.contains(modelKey);
	}

	public synchronized void registerModel(String modelKey, BufferedImage image,
										   List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads) {
		if(this.maximumSize != -1 && registeredModelMap.size() >= this.maximumSize) {
			this.deleteOldestModel();
		}
		int glId = ModelGraphicsConnector.INSTANCE.allocateAndUploadTexture(image);
		this.registerModel(modelKey, new GraphicsModel(glId, quads));
	}

	public synchronized void registerModel(String modelKey, GraphicsModel model) {
		registeredModelMap.put(modelKey, new GraphicsModelWrapper(model, System.currentTimeMillis()));
		log("Added texture " + modelKey + " (Size: " + registeredModelMap.size() + ")");
	}

	public synchronized boolean modelExists(String modelKey) {
		return registeredModelMap.containsKey(modelKey);
	}

	public synchronized GraphicsModel updateAndGetModel(String modelKey) {
		GraphicsModel model = validateAndGetModel(modelKey);
		this.registeredModelMap.get(modelKey).lastUpdated = System.currentTimeMillis();
		return model;
	}

	private synchronized void deleteModel(String modelKey) {
		if (registeredModelMap.containsKey(modelKey)) {
			GraphicsModel model = registeredModelMap.get(modelKey).model;
			registeredModelMap.remove(modelKey);
			ModelGraphicsConnector.INSTANCE.glDeleteTexture(model.getTextureGlId());
			log("Deleted texture " + modelKey);
		}
	}

	private void deleteOldestModel() {
		String oldestKey = null; long oldest = Long.MAX_VALUE;
		for (Map.Entry<String, GraphicsModelWrapper> entry : registeredModelMap.entrySet()) {
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

	public void cleanup() {
		long now = System.currentTimeMillis();
		ArrayList<String> deleteList = new ArrayList<>();
		synchronized (this) {
			for (Map.Entry<String, GraphicsModelWrapper> entry : registeredModelMap.entrySet()) {
				GraphicsModelWrapper wrapper = entry.getValue();
				if(wrapper.lastUpdated + this.expireMilliseconds < now) {
					deleteList.add(entry.getKey());
				}
			}
		}
		if(!deleteList.isEmpty()) log("Cleaning up...");
		for(String modelKey : deleteList) {
			this.deleteModel(modelKey);
		}
	}

	public synchronized void clearTextureRenderQueue() {
		imageRenderQueue.clear();
	}

	public synchronized void registerAllModelsInQueue() {
		Queue<TextureRenderQueueElement> newList = new ArrayDeque<>();

		for (int i = 0; i < REGISTER_AT_A_TIME && !imageRenderQueue.isEmpty(); i++) {
			TextureRenderQueueElement element = imageRenderQueue.poll();
			if (element == null) continue;

			BufferedImage image = element.image;
			try {
				if (image != null) {
					this.registerModel(element.key, image, element.quads);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Put the image data back to the queue if something went wrong
				newList.offer(element);
			}
		}
		imageRenderQueue = newList;
	}

	@AllArgsConstructor
	private static class TextureRenderQueueElement {
		private String key;
		private BufferedImage image;
		private List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads;
	}

	@AllArgsConstructor
	private static class GraphicsModelWrapper {
		private final GraphicsModel model;
		private long lastUpdated;
	}
}
