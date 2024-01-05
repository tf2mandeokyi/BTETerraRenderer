package com.mndk.bteterrarenderer.core.util.processor.block;

public interface ThrowableBiFunction<Key, Input, Output> {
    Output apply(Key key, Input input) throws Exception;
}
