package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;

public abstract class ImmediateBlock<Key, Input, Output> extends ProcessingBlock<Key, Input, Output> {

    @Override
    public void insert(BlockPayload<Key, Input> payload) {
        try {
            this.process(payload);
        } catch(Exception e) {
            Loggers.get(this).error("Caught exception while processing a resource (Key=" + payload + ")", e);
            this.onProcessingFail(payload, e);
        }
    }

    public static <K, I, O> ImmediateBlock<K, I, O> of(ThrowableBiFunction<K, I, O> function) {
        return new ImmediateBlock<K, I, O>() {
            protected O processInternal(K key, I input) throws Exception {
                return function.apply(key, input);
            }
        };
    }
}
