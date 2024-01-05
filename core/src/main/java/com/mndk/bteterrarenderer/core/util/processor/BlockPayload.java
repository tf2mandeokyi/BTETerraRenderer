package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import lombok.Getter;

import javax.annotation.Nullable;

public class BlockPayload<Key, T> {
    private final CacheableProcessorModel<Key, ?, ?> parent;
    private int blockIndex = 0;

    @Getter private final Key key;
    private Object payload;
    @Getter private Exception error = null;

    public BlockPayload(CacheableProcessorModel<Key, ?, ?> parent, Key key, Object payload) {
        this.parent = parent;
        this.key = key;
        this.payload = payload;
    }

    public T getPayload() {
        return BTRUtil.uncheckedCast(this.payload);
    }

    public void proceed(Object payload, @Nullable Exception error) {
        this.payload = payload;
        this.error = error;
        blockIndex++;

        if(this.error != null) {
            this.parent.onProcessingDone(this.key, null, this.error);
            return;
        }
        if(this.payload == null) {
            return;
        }
        if(blockIndex >= this.parent.blocks.size()) {
            this.parent.onProcessingDone(this.key, BTRUtil.uncheckedCast(this.payload), null);
            return;
        }

        this.parent.blocks.get(blockIndex).insert(BTRUtil.uncheckedCast(this));
    }
}
