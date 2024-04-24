package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.Loggers;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProcessorCacheStorage<K, V> implements Closeable {

    private void log(String message) {
        if(this.debug) Loggers.get(this.getClass()).info(message);
    }

    /** How long can a cache live without being refreshed. Set to -1 for no limits */
    private final long expireMilliseconds;
    /** Maximum cache size. Set to -1 for no limits */
    private final int maximumSize;
    protected final boolean debug;
    private boolean closed = false;
    private final Map<CacheableProcessorModel<K, ?, V>, List<K>> references = new HashMap<>();
    private final Map<K, CacheWrapper> map = new HashMap<>();

    @Nonnull
    private synchronized CacheWrapper getWrapper(K key) {
        return map.computeIfAbsent(key, k -> new CacheWrapper(ProcessingState.NOT_PROCESSED, null, -1, null, null));
    }

    @Nonnull
    synchronized ProcessingState getKeyState(K key) {
        return this.getWrapper(key).state;
    }

    @Nullable
    synchronized Exception getKeyErrorReason(K key) {
        return this.getWrapper(key).error;
    }

    /**
     * Returns the value and refreshes its last updated time
     * @return The value.
     * @throws NullPointerException If the value does not exist, yet has been processed, or an error was thrown
     *                              while processing it.
     */
    synchronized V updateAndGetValue(K key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if(wrapper.state != ProcessingState.PROCESSED) throw new NullPointerException();

        V value = wrapper.value;
        wrapper.lastUpdated = System.currentTimeMillis();
        return value;
    }

    synchronized void setKeyInProcessingState(CacheableProcessorModel<K, ?, V> processorModel, K key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if(wrapper.state != ProcessingState.NOT_PROCESSED) return;
        wrapper.state = ProcessingState.PROCESSING;

        List<K> keys = references.get(processorModel);
        if(keys != null) keys.add(key);
        log("Marked PROCESSING for key=" + key + " (Size: " + map.size() + ")");
    }

    synchronized void storeValue(CacheableProcessorModel<K, ?, V> processorModel,
                                 K key, V value, @Nullable Exception error,
                                 @Nullable Consumer<?> deletingFunction) {
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

        // Delete if not available
        if(this.closed || !this.references.containsKey(processorModel)) deleteValueByKey(key);
    }

    /** Deletes the wrapper by given key without checking if it's deletable */
    private synchronized void deleteValueByKey(K key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if(wrapper.deletingFunction != null) {
            Object value = wrapper.value;
            wrapper.deletingFunction.accept(BTRUtil.uncheckedCast(value));
        }

        map.remove(key);
        log("Deleted value of key=" + key + " (Size: " + map.size() + ")");
    }

    private synchronized void deleteOldest() {
        K oldestKey = null;
        long oldest = Long.MAX_VALUE;

        for(Map.Entry<K, CacheWrapper> entry : map.entrySet()) {
            CacheWrapper wrapper = entry.getValue();
            if(wrapper.state != ProcessingState.PROCESSED) continue;
            if(wrapper.lastUpdated < oldest) {
                oldestKey = entry.getKey();
                oldest = wrapper.lastUpdated;
            }
        }
        if (oldestKey == null) return;
        this.deleteValueByKey(oldestKey);
    }

    synchronized void addReference(CacheableProcessorModel<K, ?, V> processorModel) {
        this.references.put(processorModel, new ArrayList<>());
        log(String.format("Reference added for hash=%08x from hash=%08x (ref count=%d)",
                this.hashCode(), processorModel.hashCode(), this.references.size()));
    }

    synchronized void deleteReference(CacheableProcessorModel<K, ?, V> processorModel) {
        this.references.get(processorModel).forEach(this::deleteValueByKey);
        this.references.remove(processorModel);
        log(String.format("Reference lost for hash=%08x from hash=%08x (ref count=%d)",
                this.hashCode(), processorModel.hashCode(), this.references.size()));
    }

    public synchronized void cleanUp() {
        long now = System.currentTimeMillis();
        ArrayList<K> deleteList = new ArrayList<>();
        map.forEach((key, cacheWrapper) -> {
            if (cacheWrapper.state != ProcessingState.PROCESSED) return;
            if (this.expireMilliseconds == -1 || cacheWrapper.lastUpdated + this.expireMilliseconds > now) return;
            deleteList.add(key);
        });
        if(deleteList.isEmpty()) return;

        log("Cleaning up...");
        for (K key : deleteList) {
            this.deleteValueByKey(key);
        }
    }

    @Override
    public synchronized void close() {
        // Cleanup all values
        ArrayList<K> deleteList = new ArrayList<>();
        for(Map.Entry<K, CacheWrapper> entry : map.entrySet()) {
            CacheWrapper wrapper = entry.getValue();
            if(wrapper.state != ProcessingState.PROCESSED) continue;
            deleteList.add(entry.getKey());
        }

        log("Closing...");
        for (K key : deleteList) {
            this.deleteValueByKey(key);
        }
        this.closed = true;
    }

    @AllArgsConstructor
    private class CacheWrapper {
        private ProcessingState state;
        @Nullable
        private V value;
        private long lastUpdated;
        @Nullable
        private Exception error;
        @Nullable
        private Consumer<?> deletingFunction;
    }
}
