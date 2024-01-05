package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPayload<Key, T> {
    private final CacheableProcessorModel<Key, ?, ?> parent;
    private int blockIndex = 0;

    @Getter private final Key key;
    @Nonnull private Object payload;

    public BlockPayload(CacheableProcessorModel<Key, ?, ?> parent, Key key, @Nonnull Object payload) {
        this.parent = parent;
        this.key = key;
        this.payload = payload;
    }

    public T getPayload() {
        return BTRUtil.uncheckedCast(this.payload);
    }

    public void proceed(@Nullable Object payload, @Nullable Exception error) {
        if(error != null) {
            this.parent.onProcessingDone(this.key, null, error);
            return;
        }
        if(payload == null) return;

        this.payload = payload;
        blockIndex++;
        if(blockIndex >= this.parent.blocks.size()) {
            this.parent.onProcessingDone(this.key, BTRUtil.uncheckedCast(this.payload), null);
            return;
        }

        this.parent.blocks.get(blockIndex).insert(BTRUtil.uncheckedCast(this));
    }
}
