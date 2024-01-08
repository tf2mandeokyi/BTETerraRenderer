package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

@Setter
public abstract class ProcessingBlock<Key, Input, Output> {

    @Getter
    private final boolean closeableByModel;
    protected boolean debug = false;

    protected ProcessingBlock(boolean closeableByModel) {
        this.closeableByModel = closeableByModel;
    }

    protected final void onProcessingFail(BlockPayload<Key, Input> payload, Exception error) {
        Loggers.get(this).error("Caught exception while processing a resource (Key=" + payload + ")", error);
        payload.proceed(null, error);
    }

    protected final void process(BlockPayload<Key, Input> payload) throws Exception {
        Output output = this.processInternal(payload.getKey(), payload.getPayload());
        payload.proceed(output, null);
    }

    public abstract void insert(BlockPayload<Key, Input> payload);

    /**
     * @return {@code null} if the resource is not yet ready
     */
    protected abstract Output processInternal(Key key, @Nonnull Input input) throws Exception;
}
