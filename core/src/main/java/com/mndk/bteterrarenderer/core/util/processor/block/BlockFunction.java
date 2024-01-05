package com.mndk.bteterrarenderer.core.util.processor.block;

import javax.annotation.Nonnull;

public interface BlockFunction<Key, Input, Output> {
    /** @see ProcessingBlock#processInternal */
    Output apply(Key key, @Nonnull Input input) throws Exception;
}
