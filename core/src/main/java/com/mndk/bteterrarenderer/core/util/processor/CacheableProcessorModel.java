package com.mndk.bteterrarenderer.core.util.processor;

import com.google.common.collect.ImmutableList;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.block.ProcessingBlock;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class CacheableProcessorModel<Key, Input, Output> {

    private void log(String message) {
        if(this.debug) Loggers.get(this.getClass()).info(message);
    }

    private final int maximumSize;
    protected final boolean debug;
    private final long expireMilliseconds;
    ImmutableList<ProcessingBlock<Key, ?, ?>> blocks = null; // late init

    private final Map<Key, CacheWrapper> resourceWrapperMap = new HashMap<>();

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed. Set to -1 for no limits
     * @param maximumSize Maximum cache size. Set to -1 for no limits
     * @param debug debug
     */
    protected CacheableProcessorModel(long expireMilliseconds, int maximumSize, boolean debug) {
        this.expireMilliseconds = expireMilliseconds;
        this.maximumSize = maximumSize;
        this.debug = debug;
    }

    @Nonnull
    private synchronized CacheWrapper getWrapper(Key key) {
        return resourceWrapperMap.computeIfAbsent(key, k -> new CacheWrapper(ProcessingState.NOT_PROCESSED, null, -1, null));
    }

    @Nonnull
    public ProcessingState getResourceProcessingState(Key key) {
        return this.getWrapper(key).state;
    }

    @Nullable
    @SuppressWarnings("unused")
    public Exception getResourceErrorReason(Key key) {
        return this.getWrapper(key).error;
    }

    /**
     * Returns the resource and refreshes its last updated time
     * @return The resource.
     * @throws NullPointerException If the resource does not exist, yet has been processed, or an error was thrown
     *                              while processing it.
     */
    public Output updateAndGetOutput(Key key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if(wrapper.state != ProcessingState.PROCESSED) throw new NullPointerException();

        Output resource = wrapper.output;
        wrapper.lastUpdated = System.currentTimeMillis();
        return resource;
    }

    public void insertInput(Key key, Input input) {
        CacheWrapper wrapper = this.getWrapper(key);
        wrapper.state = ProcessingState.PROCESSING;

        if(this.blocks == null) this.blocks = this.getSequentialBuilder().build();
        this.blocks.get(0).insert(new BlockPayload<>(this, key, input));
    }

    /**
     * Returns the output resource if it exists. Else, it inserts the result of the {@code computeInputIfAbsent} function
     * and returns {@code null}.
     */
    @Nullable
    public Output updateOrInsert(Key key, Supplier<Input> computeInputIfAbsent) {
        ProcessingState state = this.getResourceProcessingState(key);
        switch(state) {
            case PROCESSED:
                return this.updateAndGetOutput(key);
            case NOT_PROCESSED:
                this.insertInput(key, computeInputIfAbsent.get());
                break;
        }
        return null;
    }

    public void onProcessingDone(Key key, Output output, @Nullable Exception error) {
        synchronized(resourceWrapperMap) {
            if (this.maximumSize != -1 && resourceWrapperMap.size() >= this.maximumSize) {
                this.deleteOldestResource();
            }
        }

        CacheWrapper wrapper = this.getWrapper(BTRUtil.uncheckedCast(key));
        wrapper.output = output;
        wrapper.lastUpdated = System.currentTimeMillis();
        wrapper.state = error != null ? ProcessingState.ERROR : ProcessingState.PROCESSED;
        wrapper.error = error;

        synchronized(resourceWrapperMap) {
            log("Added resource for " + key + " (Size: " + resourceWrapperMap.size() + ")");
        }
    }

    private void deleteResourceByKey(Key key) {
        CacheWrapper wrapper = this.getWrapper(key);
        if(wrapper.state != ProcessingState.PROCESSED) return;

        Output output = wrapper.output;
        this.deleteResource(output);
        synchronized(resourceWrapperMap) {
            resourceWrapperMap.remove(key);
            log("Deleted resource " + key + " (Size: " + resourceWrapperMap.size() + ")");
        }
    }

    private void deleteOldestResource() {
        Key oldestKey = null;
        long oldest = Long.MAX_VALUE;

        synchronized(resourceWrapperMap) {
            for (Map.Entry<Key, CacheWrapper> entry : resourceWrapperMap.entrySet()) {
                CacheWrapper wrapper = entry.getValue();
                if (wrapper.state != ProcessingState.PROCESSED) continue;
                if (wrapper.lastUpdated < oldest) {
                    oldestKey = entry.getKey();
                    oldest = wrapper.lastUpdated;
                }
            }
        }
        if (oldestKey == null) return;

        this.deleteResourceByKey(oldestKey);
    }

    public void cleanUp() {
        long now = System.currentTimeMillis();
        ArrayList<Key> deleteList = new ArrayList<>();

        synchronized(resourceWrapperMap) {
            for (Map.Entry<Key, CacheWrapper> entry : resourceWrapperMap.entrySet()) {
                CacheWrapper wrapper = entry.getValue();
                if (wrapper.state != ProcessingState.PROCESSED) continue;
                if (this.expireMilliseconds == -1 || wrapper.lastUpdated + this.expireMilliseconds > now) continue;
                deleteList.add(entry.getKey());
            }
        }
        if (deleteList.isEmpty()) return;

        log("Cleaning up...");
        for (Key key : deleteList) {
            this.deleteResourceByKey(key);
        }
    }

    /**
     * This is called only once in the constructor.
     */
    protected abstract SequentialBuilder<Key, Input, Output> getSequentialBuilder();
    protected abstract void deleteResource(Output output);

    @AllArgsConstructor
    private class CacheWrapper {
        private ProcessingState state;
        @Nullable
        private Output output;
        private long lastUpdated;
        @Nullable
        private Exception error;
    }

    public static class SequentialBuilder<Key, Initial, T> {
        private final List<ProcessingBlock<Key, ?, ?>> blocks = new ArrayList<>();

        public SequentialBuilder(@Nonnull ProcessingBlock<Key, Initial, T> firstBlock) {
            this.blocks.add(firstBlock);
        }

        public <U> SequentialBuilder<Key, Initial, U> then(@Nonnull ProcessingBlock<Key, T, U> nextBlock) {
            this.blocks.add(nextBlock);
            return BTRUtil.uncheckedCast(this);
        }

        public ImmutableList<ProcessingBlock<Key, ?, ?>> build() {
            return ImmutableList.copyOf(blocks);
        }
    }
}
