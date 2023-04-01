package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.connector.Connectors;
import lombok.AllArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Cache class that stores tile image data.
 */
public class TileImageCacheManager {

	private static final int CACHE_AT_A_TIME = 5;

	private static final TileImageCacheManager instance = new TileImageCacheManager(1000 * 60 * 5, 10000);

	public static TileImageCacheManager getInstance() { return instance; }

	private static final boolean DEBUG = false;

	private static void log(String message) {
		if(DEBUG) BTETerraRendererCore.logger.debug(message);
	}

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

	private int validateAndGetGlId(String tileKey) {
		synchronized (this) {
			if (!glTextureIdMap.containsKey(tileKey)) throw new NullPointerException();
			return glTextureIdMap.get(tileKey).glId;
		}
	}

	public void tileIsBeingDownloaded(String tileKey) {
		synchronized (this) {
			downloadingTileKeys.add(tileKey);
		}
	}

	public void tileDownloadingComplete(String tileKey, BufferedImage image) {
		synchronized (this) {
			downloadingTileKeys.remove(tileKey);
			imageRenderQueue.add(new AbstractMap.SimpleEntry<>(tileKey, image));
		}
	}

	public boolean isTileInDownloadingState(String tileKey) {
		synchronized (this) {
			return downloadingTileKeys.contains(tileKey);
		}
	}

	public void addTexture(String tileKey, BufferedImage image) {
		synchronized (this) {
			if(this.maximumSize != -1 && glTextureIdMap.size() >= this.maximumSize) {
				this.deleteOldestTexture();
			}
			this.addTexture(tileKey, initializeTile(image));
		}
	}

	public void addTexture(String tileKey, int glId) {
		synchronized (this) {
			glTextureIdMap.put(tileKey, new GLIdWrapper(glId, System.currentTimeMillis()));
			log("Added texture " + tileKey + " (Size: " + glTextureIdMap.size() + ")");
		}
	}

	public boolean textureExists(String tileKey) {
		synchronized (this) {
			return glTextureIdMap.containsKey(tileKey);
		}
	}

	public void bindTexture(String tileKey) {
		synchronized (this) {
			int glId = validateAndGetGlId(tileKey);
			this.glTextureIdMap.get(tileKey).lastUpdated = System.currentTimeMillis();
			Connectors.GRAPHICS.glBindTexture(glId);
		}
	}

	private void deleteTexture(String tileKey) {
		synchronized (this) {
			if (glTextureIdMap.containsKey(tileKey)) {
				int glId = glTextureIdMap.get(tileKey).glId;
				glTextureIdMap.remove(tileKey);
				Connectors.GRAPHICS.glDeleteTexture(glId);
				log("Deleted texture " + tileKey);
			}
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

	/**
	 * @return The GL texture id associated with the image texture.
	 * */
	private int initializeTile(BufferedImage image) {
		int width = image.getWidth(), height = image.getHeight();
		int glTextureId = Connectors.GRAPHICS.glGenTextures();

		int[] imageData = new int[width * height];
		image.getRGB(0, 0, width, height, imageData, 0, width);

		Connectors.GRAPHICS.glAllocateTexture(glTextureId, width, height);
		Connectors.GRAPHICS.glUploadTexture(glTextureId, imageData, width, height);

		return glTextureId;
	}

	public void deleteAllRenderQueues() {
		synchronized (this) {
			imageRenderQueue.clear();
		}
	}

	public void cacheAllImagesInQueue() {
		List<Map.Entry<String, BufferedImage>> newList = new ArrayList<>();

		synchronized (this) {
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

	}

	@AllArgsConstructor
	private static class GLIdWrapper {
		final int glId;
		long lastUpdated;
	}
}
