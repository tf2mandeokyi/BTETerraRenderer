package com.mndk.bteterrarenderer.core.util.processor;

import com.google.common.collect.ImmutableList;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.processor.block.ProcessingBlock;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class CacheableProcessorModel<Key, Input, Output> implements Closeable {

    @Getter
    private boolean closed = false;
    private final ProcessorCacheStorage<Key, Output> storage;
    ImmutableList<ProcessingBlock<Key, ?, ?>> blocks = null; // late init

    protected CacheableProcessorModel(ProcessorCacheStorage<Key, Output> storage) {
        this.storage = storage;
        this.storage.addReference(this);
    }

    @Nonnull
    public synchronized ProcessingState getResourceProcessingState(Key key) {
        return this.storage.getKeyState(key);
    }

    @Nullable
    @SuppressWarnings("unused")
    public synchronized Exception getResourceErrorReason(Key key) {
        return this.storage.getKeyErrorReason(key);
    }

    /**
     * Returns the resource and refreshes its last updated time
     * @return The resource.
     * @throws NullPointerException If the resource does not exist, yet has been processed, or an error was thrown
     *                              while processing it.
     */
    public Output updateAndGetOutput(Key key) {
        return this.storage.updateAndGetValue(key);
    }

    public synchronized void insertInput(Key key, Input input) {
        this.storage.setKeyInProcessingState(this, key);
        if (this.blocks == null) this.blocks = this.getSequentialBuilder().build();
        this.blocks.get(0).insert(new BlockPayload<>(this, key, input));
    }

    /**
     * Returns the output resource if it exists. Else, it inserts the result of the {@code computeInputIfAbsent} function
     * and returns {@code null}.
     */
    @Nullable
    public synchronized Output updateOrInsert(Key key, Supplier<Input> computeInputIfAbsent) {
        ProcessingState state = this.storage.getKeyState(key);
        switch (state) {
            case PROCESSED:
                return this.updateAndGetOutput(key);
            case NOT_PROCESSED:
                this.insertInput(key, computeInputIfAbsent.get());
                break;
        }
        return null;
    }

    /**
     * Returns the output resource if it exists. Else, it inserts the input and returns {@code null}.
     */
    @Nullable
    public synchronized Output updateOrInsert(Key key, Input input) {
        ProcessingState state = this.storage.getKeyState(key);
        switch (state) {
            case PROCESSED:
                return this.updateAndGetOutput(key);
            case NOT_PROCESSED:
                this.insertInput(key, input);
                break;
        }
        return null;
    }

    public synchronized void onProcessingDone(Key key, Output output, @Nullable Exception error) {
        this.storage.storeValue(this, key, output, error, this::deleteResource);
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public synchronized void close() throws IOException {
        // Close blocks
        for (ProcessingBlock<Key, ?, ?> block : blocks) {
            if (!block.isCloseableByModel()) continue;
            if (!(block instanceof Closeable)) continue;
            ((Closeable) block).close();
        }
        this.closed = true;
        this.storage.deleteReference(this);
    }

    /**
     * This is called only once in the constructor.
     */
    protected abstract SequentialBuilder<Key, Input, Output> getSequentialBuilder();
    protected abstract void deleteResource(Output output);

    public static <Key, Initial, T> SequentialBuilder<Key, Initial, T> builder(@Nonnull ProcessingBlock<Key, Initial, T> firstBlock) {
        return new SequentialBuilder<>(firstBlock);
    }

    public static class SequentialBuilder<Key, Initial, T> {
        private final List<ProcessingBlock<Key, ?, ?>> blocks = new ArrayList<>();

        private SequentialBuilder(@Nonnull ProcessingBlock<Key, Initial, T> firstBlock) {
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
