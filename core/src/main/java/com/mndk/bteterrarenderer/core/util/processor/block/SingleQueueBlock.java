package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class SingleQueueBlock<Key, Input, Output> extends ProcessingBlock<Key, Input, Output> {

    private final Queue<BlockPayload<Key, Input>> queue = new ArrayDeque<>();

    protected SingleQueueBlock() {
        super(false);
    }

    @Override
    public void insert(BlockPayload<Key, Input> payload) {
        this.queue.add(payload);
    }

    public void process(int processAtATime) {
        for(int i = 0; i < processAtATime && !queue.isEmpty(); i++) {
            BlockPayload<Key, Input> payload = queue.poll();
            if (payload == null) continue;

            try {
                this.process(payload);
            } catch(Exception e) {
                this.onProcessingFail(payload, e);
            }
        }
    }

    public static <K, I, O> SingleQueueBlock<K, I, O> of(BlockFunction<K, I, O> function) {
        return new SingleQueueBlock<K, I, O>() {
            protected O processInternal(K key, @Nonnull I input) throws Exception {
                return function.apply(key, input);
            }
        };
    }
}
