package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class SingleQueueBlock<Key, Input, Output> extends ProcessingBlock<Key, Input, Output> {

    private final Queue<BlockPayload<Key, Input>> queue = new ArrayDeque<>();

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
                Loggers.get(this).error("Caught exception while processing a resource (Key=" + payload.getKey() + ")", e);
                this.onProcessingFail(payload, e);
            }
        }
    }
}
