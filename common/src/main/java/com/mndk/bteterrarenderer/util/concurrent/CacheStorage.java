package com.mndk.bteterrarenderer.util.concurrent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.Loggers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class CacheStorage<K, V> implements Closeable {

    public static final long DEFAULT_CACHE_EXPIRE_MILLISECONDS = 1000 * 60 * 30; // 30 minutes
    public static final int DEFAULT_CACHE_MAXIMUM_SIZE = -1;
    public static final boolean DEFAULT_CACHE_DEBUG = false;

    private void log(String message) {
        if (this.debug) Loggers.get(this.getClass()).info(message);
    }

    /** How long can a cache live without being refreshed. Set to -1 for no limits */
    private final long expireMilliseconds;
    /** Maximum cache size. Set to -1 for no limits */
    private final int maximumSize;
    protected final boolean debug;

    private final AtomicInteger processingCount = new AtomicInteger(0);
    private final Map<K, CacheWrapper> map = new ConcurrentHashMap<>();

    /**
     * Constructs a ProcessorCacheStorage with the specified configuration.
     *
     * @param config The configuration for the cache storage. If {@code null},
     *               default values will be used.
     */
    public CacheStorage(@Nullable Config config) {
        Long expireMilliseconds = config != null ? config.getExpireMilliseconds() : null;
        Integer maximumSize = config != null ? config.getMaximumSize() : null;
        Boolean debug = config != null ? config.getDebug() : null;
        this.expireMilliseconds = expireMilliseconds != null ? expireMilliseconds : DEFAULT_CACHE_EXPIRE_MILLISECONDS;
        this.maximumSize = maximumSize != null ? maximumSize : DEFAULT_CACHE_MAXIMUM_SIZE;
        this.debug = debug != null ? debug : DEFAULT_CACHE_DEBUG;
    }

    protected V whenProcessing(K key) { return null; }
    protected V whenError(K key, Throwable error) { return null; }
    protected void delete(V value) {}

    public int getProcessingCount() {
        return processingCount.get();
    }

    @Nullable
    public final V getOrCompute(K key, @Nonnull Supplier<CompletableFuture<V>> function) {
        CacheWrapper wrapper = this.getWrapper(key);
        switch (wrapper.state) {
            case NOT_PROCESSED:
                wrapper.state = ProcessingState.PROCESSING;
                this.processingCount.incrementAndGet();

                CompletableFuture<V> future = function.get();
                if (future == null) {
                    this.processingCount.decrementAndGet();
                    wrapper.state = ProcessingState.NOT_PROCESSED;
                    return null;
                }

                future.whenComplete((models, error) -> {
                    this.storeValue(key, models, error, this::delete);
                    this.processingCount.decrementAndGet();
                    if (error != null) Loggers.get(this).error("Error processing cache value", error);
                });
                return null;
            case PROCESSED: return this.updateAndGetValue(key);
            case PROCESSING: return this.whenProcessing(key);
            case ERROR: return this.whenError(key, this.getWrapper(key).error);
        }
        throw new IllegalStateException("Invalid processing state: " + wrapper.state);
    }

    /**
     * Retrieves the cache wrapper for the given key, creating a new one if it does not exist.
     *
     * @param key The key for which to retrieve the cache wrapper.
     * @return The cache wrapper associated with the given key.
     */
    @Nonnull
    private CacheWrapper getWrapper(K key) {
        return map.computeIfAbsent(key, k -> new CacheWrapper(ProcessingState.NOT_PROCESSED, null, -1, null, null));
    }

    /**
     * Returns the output resource and refreshes its last updated time.
     *
     * @param key The key for which to retrieve the output resource.
     * @return The output resource.
     * @throws NullPointerException If the resource does not exist, yet has been processed, or an error was thrown
     *                              while processing it.
     */
    private V updateAndGetValue(K key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if (wrapper.state != ProcessingState.PROCESSED) throw new NullPointerException();

        V value = wrapper.value;
        wrapper.lastUpdated = System.currentTimeMillis();
        return value;
    }

    /**
     * Stores the value associated with the given key in the cache.
     * If the cache size exceeds the maximum size, the oldest entry is deleted.
     *
     * @param key            The key to store the value for.
     * @param value          The value to store.
     * @param error          The error associated with the key, if any.
     * @param deletingFunction The function to call when deleting the value, if any.
     */
    private void storeValue(K key, V value, @Nullable Throwable error, @Nullable Consumer<V> deletingFunction) {
        if (this.maximumSize != -1 && map.size() >= this.maximumSize) {
            this.deleteOldest();
        }

        CacheWrapper wrapper = this.getWrapper(BTRUtil.uncheckedCast(key));
        wrapper.lastUpdated = System.currentTimeMillis();
        wrapper.state = error != null ? ProcessingState.ERROR : ProcessingState.PROCESSED;
        wrapper.value = value;
        wrapper.error = error;
        wrapper.deletingFunction = deletingFunction;

        log("Added value[" + wrapper.state + "] of key=" + key + " (Size: " + map.size() + ")");
    }

    /**
     * Deletes the value associated with the given key from the cache.
     *
     * @param key The key for which to delete the value.
     */
    private void deleteValueByKey(K key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if (wrapper.deletingFunction != null) {
            wrapper.deletingFunction.accept(wrapper.value);
        }

        map.remove(key);
        log("Deleted value of key=" + key + " (Size: " + map.size() + ")");
    }

    /**
     * Deletes the oldest processed value from the cache.
     * This method is called when the cache size exceeds the maximum size.
     */
    private void deleteOldest() {
        K oldestKey = null;
        long oldest = Long.MAX_VALUE;

        for (Map.Entry<K, CacheWrapper> entry : map.entrySet()) {
            CacheWrapper wrapper = entry.getValue();
            if (wrapper.state != ProcessingState.PROCESSED) continue;
            if (wrapper.lastUpdated < oldest) {
                oldestKey = entry.getKey();
                oldest = wrapper.lastUpdated;
            }
        }
        if (oldestKey == null) return;
        this.deleteValueByKey(oldestKey);
    }

    /**
     * Cleans up expired cache entries.
     * This method iterates through the cache and removes entries that have expired based on the configured expiration time.
     * If the expiration time is set to -1, no entries will be removed.
     */
    public void cleanUp() {
        long now = System.currentTimeMillis();
        ArrayList<K> deleteList = new ArrayList<>();
        map.forEach((key, cacheWrapper) -> {
            if (cacheWrapper.state != ProcessingState.PROCESSED) return;
            if (this.expireMilliseconds == -1 || cacheWrapper.lastUpdated + this.expireMilliseconds > now) return;
            deleteList.add(key);
        });
        if (deleteList.isEmpty()) return;

        log("Cleaning up...");
        for (K key : deleteList) {
            this.deleteValueByKey(key);
        }
    }

    /**
     * Closes the cache storage and cleans up all processed values.
     * This method iterates through the cache and removes all processed entries.
     * If a deleting function is associated with a value, it will be called.
     */
    @Override
    public void close() {
        // Cleanup all values
        ArrayList<K> deleteList = new ArrayList<>();
        for (Map.Entry<K, CacheWrapper> entry : map.entrySet()) {
            CacheWrapper wrapper = entry.getValue();
            if (wrapper.state != ProcessingState.PROCESSED) continue;
            deleteList.add(entry.getKey());
        }

        log("Closing...");
        for (K key : deleteList) {
            this.deleteValueByKey(key);
        }
    }

    @AllArgsConstructor
    private class CacheWrapper {
        private ProcessingState state;
        @Nullable
        private V value;
        private long lastUpdated;
        @Nullable
        private Throwable error;
        @Nullable
        private Consumer<V> deletingFunction;
    }

    @Getter
    @JsonDeserialize
    public static class Config {
        @Nullable private final Long expireMilliseconds;
        @Nullable private final Integer maximumSize;
        @Nullable private final Boolean debug;

        /**
         * Configuration class for ProcessorCacheStorage.
         *
         * @param expireMilliseconds The time in milliseconds after which a cache entry expires.
         *                           If {@code null}, defaults to {@link #DEFAULT_CACHE_EXPIRE_MILLISECONDS}.
         * @param maximumSize        The maximum number of cache entries. If {@code null},
         *                           defaults to {@link #DEFAULT_CACHE_MAXIMUM_SIZE}.
         * @param debug              Whether to enable debug logging. If {@code null},
         *                           defaults to {@link #DEFAULT_CACHE_DEBUG}.
         */
        @JsonCreator
        public Config(
                @Nullable @JsonProperty(value = "expire_milliseconds") Long expireMilliseconds,
                @Nullable @JsonProperty(value = "maximum_size") Integer maximumSize,
                @Nullable @JsonProperty(value = "debug") Boolean debug
        ) {
            this.expireMilliseconds = expireMilliseconds != null ? expireMilliseconds : DEFAULT_CACHE_EXPIRE_MILLISECONDS;
            this.maximumSize = maximumSize != null ? maximumSize : DEFAULT_CACHE_MAXIMUM_SIZE;
            this.debug = debug != null ? debug : DEFAULT_CACHE_DEBUG;
        }
    }
}
