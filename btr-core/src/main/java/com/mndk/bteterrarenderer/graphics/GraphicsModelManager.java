package com.mndk.bteterrarenderer.graphics;

import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnector;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Model manager class that stores models & texture data.
 */
public class GraphicsModelManager {

	public static final GraphicsModelManager INSTANCE =
			new GraphicsModelManager(1000 * 60 * 5 /* 5 minutes */, 10000);

	private static final Logger LOGGER = LogManager.getLogger();
	private static final boolean DEBUG = false;
	private static void log(String message) {
		if(DEBUG) LOGGER.info(message);
	}

	private static final int BAKE_AT_A_TIME = 5;

	private final int maximumSize;
	private final Map<Object, GraphicsModelWrapper> registeredModelMap = new HashMap<>();
	private final Set<Object> downloadingKeys = new HashSet<>(), bakingKeys = new HashSet<>(), errorKeys = new HashSet<>();
	private final long expireMilliseconds;
	private QueueNode currentQueueNode = new QueueNode();
	private int queueCount = 1;

	public GraphicsModelManager(long expireMilliseconds, int maximumSize) {
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

	private synchronized void offerQueue(TextureBakeRequest request) {
		this.updateQueueNode();
		currentQueueNode.offer(request);
	}

	public synchronized void newQueue() {
		long now = System.currentTimeMillis();
		if(!currentQueueNode.isExpired(now) && !currentQueueNode.isEmpty()) {
			QueueNode newQueue = new QueueNode();
			newQueue.previous = this.currentQueueNode;
			this.currentQueueNode = newQueue;
			log("Starting new queue (Queue count: " + ++queueCount + ")");
		}
	}

	private synchronized void bakeModel(Object modelKey, BufferedImage image,
									   List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads) {
		if(this.maximumSize != -1 && registeredModelMap.size() >= this.maximumSize) {
			this.deleteOldestModel();
		}
		int glId = ModelGraphicsConnector.INSTANCE.allocateAndUploadTexture(image);
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
			ModelGraphicsConnector.INSTANCE.glDeleteTexture(model.getTextureGlId());
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

	public synchronized void bakeModelsInQueue() {
		this.currentQueueNode.bakeModelsInQueue();
		this.updateQueueNode();
	}

	private synchronized void updateQueueNode() {
		long now = System.currentTimeMillis();
		while(currentQueueNode.isExpired(now)) {
			currentQueueNode = currentQueueNode.previous;
			log("Queue expired (Queue count: " + --queueCount + ")");
		}
	}

	private class QueueNode {
		private static final long NODE_EXPIRE_TIME = 1000 * 3; // 3 seconds

		private final Queue<TextureBakeRequest> queue = new ArrayDeque<>();
		private QueueNode previous = null;
		private long timeSinceEmptyMilliseconds = -1;

		private synchronized void offer(TextureBakeRequest request) {
			this.queue.offer(request);
			this.timeSinceEmptyMilliseconds = -1;
		}

		private synchronized boolean isExpired(long now) {
			if(previous == null) return false;
			return this.timeSinceEmptyMilliseconds != -1 && now - timeSinceEmptyMilliseconds >= NODE_EXPIRE_TIME;
		}

		private synchronized void bakeModelsInQueue() {
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

			if(queue.isEmpty() && this.timeSinceEmptyMilliseconds == -1) {
				this.timeSinceEmptyMilliseconds = System.currentTimeMillis();
			}
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}
	}

	@AllArgsConstructor
	private static class TextureBakeRequest {
		private Object key;
		private BufferedImage image;
		private List<GraphicsQuad<GraphicsQuad.PosTexColor>> quads;
	}

	@AllArgsConstructor
	private static class GraphicsModelWrapper {
		private final GraphicsModel model;
		private long lastUpdated;
	}
}
