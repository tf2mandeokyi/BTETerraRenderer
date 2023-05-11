package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Cache class that stores tile image data.
 */
public class TileImageCacheManager {

	private static final TileImageCacheManager INSTANCE = new TileImageCacheManager(1000 * 60 * 5, 10000);
	public static TileImageCacheManager getInstance() { return INSTANCE; }

	private static final boolean DEBUG = false;
	private static void log(String message) {
		if(DEBUG) BTETerraRendererConstants.LOGGER.debug(message);
	}

	private static final int CACHE_AT_A_TIME = 5;

	private final int maximumSize;
	private final Map<String, GLIdWrapper> glTextureIdMap;
	private final Set<String> downloadingTileKeys;
	private final long expireMilliseconds;
	private List<Map.Entry<String, BufferedImage>> imageRenderQueue;

	public TileImageCacheManager(long expireMilliseconds, int maximumSize) {
		this.glTextureIdMap = new HashMap<>();
		this.downloadingTileKeys = new HashSet<>();
		this.expireMilliseconds = expireMilliseconds;
		this.maximumSize = maximumSize;
		this.imageRenderQueue = new ArrayList<>();
	}

	private synchronized int validateAndGetGlId(String tileKey) {
		if (!glTextureIdMap.containsKey(tileKey)) throw new NullPointerException();
		return glTextureIdMap.get(tileKey).glId;
	}

	public synchronized void tileIsBeingDownloaded(String tileKey) {
		downloadingTileKeys.add(tileKey);
	}

	public synchronized void tileDownloadingComplete(String tileKey, BufferedImage image) {
		downloadingTileKeys.remove(tileKey);
		imageRenderQueue.add(new AbstractMap.SimpleEntry<>(tileKey, image));
	}

	public synchronized boolean isTileInDownloadingState(String tileKey) {
		return downloadingTileKeys.contains(tileKey);
	}

	public synchronized void addTexture(String tileKey, BufferedImage image) {
		if(this.maximumSize != -1 && glTextureIdMap.size() >= this.maximumSize) {
			this.deleteOldestTexture();
		}
		int glId = GraphicsConnector.INSTANCE.allocateAndUploadTileTexture(image);
		this.addTexture(tileKey, glId);
	}

	public synchronized void addTexture(String tileKey, int glId) {
		glTextureIdMap.put(tileKey, new GLIdWrapper(glId, System.currentTimeMillis()));
		log("Added texture " + tileKey + " (Size: " + glTextureIdMap.size() + ")");
	}

	public synchronized boolean textureExists(String tileKey) {
		return glTextureIdMap.containsKey(tileKey);
	}

//	public synchronized void bindTexture(String tileKey) {
//		int glId = validateAndGetGlId(tileKey);
//		this.glTextureIdMap.get(tileKey).lastUpdated = System.currentTimeMillis();
//		GraphicsConnector.INSTANCE.glBindTileTexture(glId);
//	}

	public synchronized int updateAndGetGlId(String tileKey) {
		int glId = validateAndGetGlId(tileKey);
		this.glTextureIdMap.get(tileKey).lastUpdated = System.currentTimeMillis();
		return glId;
	}

	private synchronized void deleteTexture(String tileKey) {
		if (glTextureIdMap.containsKey(tileKey)) {
			int glId = glTextureIdMap.get(tileKey).glId;
			glTextureIdMap.remove(tileKey);
			GraphicsConnector.INSTANCE.glDeleteTileTexture(glId);
			log("Deleted texture " + tileKey);
		}
	}

	private void deleteOldestTexture() {
		String oldestKey = null; long oldest = Long.MAX_VALUE;
		for (Map.Entry<String, GLIdWrapper> entry : glTextureIdMap.entrySet()) {
			GLIdWrapper wrapper = entry.getValue();
			if(wrapper.lastUpdated < oldest) {
				oldestKey = entry.getKey();
				oldest = wrapper.lastUpdated;
			}
		}
		if(oldestKey != null) {
			this.deleteTexture(oldestKey);
		}
	}

	public void cleanup() {
		long now = System.currentTimeMillis();
		ArrayList<String> deleteList = new ArrayList<>();
		synchronized (this) {
			for (Map.Entry<String, GLIdWrapper> entry : glTextureIdMap.entrySet()) {
				GLIdWrapper wrapper = entry.getValue();
				if(wrapper.lastUpdated + this.expireMilliseconds < now) {
					deleteList.add(entry.getKey());
				}
			}
		}
		if(!deleteList.isEmpty()) log("Cleaning up...");
		for(String key : deleteList) {
			this.deleteTexture(key);
		}
	}

	public synchronized void deleteAllRenderQueues() {
		imageRenderQueue.clear();
	}

	public synchronized void cacheAllImagesInQueue() {
		List<Map.Entry<String, BufferedImage>> newList = new ArrayList<>();

		for (int i = 0; i < CACHE_AT_A_TIME && !imageRenderQueue.isEmpty(); i++) {
			Map.Entry<String, BufferedImage> entry = imageRenderQueue.get(0);
			imageRenderQueue.remove(0);
			if (entry == null) continue;

			String tileKey = entry.getKey();
			BufferedImage image = entry.getValue();

			try {
				if (image != null) {
					this.addTexture(tileKey, image);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Put the image data back to the queue if something went wrong
				newList.add(entry);
			}
		}
		imageRenderQueue = newList;
	}

	@AllArgsConstructor
	private static class GLIdWrapper {
		final int glId;
		long lastUpdated;
	}
}
