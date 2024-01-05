package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;
import lombok.Setter;

import javax.annotation.Nullable;

@Setter
public abstract class ProcessingBlock<Key, Input, Output> {

    protected boolean debug = false;

    protected final void onProcessingDone(BlockPayload<Key, Input> payload, Output output, @Nullable Exception error) {
        payload.proceed(output, error);
    }

    protected final void onProcessingFail(BlockPayload<Key, Input> payload, Exception error) {
        this.onProcessingDone(payload, null, error);
    }

    protected final void process(BlockPayload<Key, Input> payload) throws Exception {
        Output output = this.processInternal(payload.getKey(), payload.getPayload());
        this.onProcessingDone(payload, output, null);
    }

    public abstract void insert(BlockPayload<Key, Input> payload);
    protected abstract Output processInternal(Key key, Input input) throws Exception;
}
