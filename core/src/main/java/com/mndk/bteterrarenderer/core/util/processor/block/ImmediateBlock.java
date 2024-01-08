package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class ImmediateBlock<Key, Input, Output> extends ProcessingBlock<Key, Input, Output> {

    protected ImmediateBlock() {
        super(false);
    }

    @Override
    public void insert(BlockPayload<Key, Input> payload) {
        try {
            this.process(payload);
        } catch(Exception e) {
            this.onProcessingFail(payload, e);
        }
    }

    public static <K, I, O> ImmediateBlock<K, I, O> of(BlockFunction<K, I, O> function) {
        return new ImmediateBlock<K, I, O>() {
            protected O processInternal(K key, @Nonnull I input) throws Exception {
                return function.apply(key, input);
            }
        };
    }

    public static <K, T> ImmediateBlock<K, T, List<T>> singletonList() {
        return of((key, input) -> Collections.singletonList(input));
    }
}
