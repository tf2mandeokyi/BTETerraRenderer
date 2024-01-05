package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.util.processor.block.MappedQueueBlock;
import io.netty.buffer.ByteBuf;

public class FlatTileResourceFetcher extends MappedQueueBlock<FlatTileKey, Integer, String, ByteBuf> {

    /**
     * @param nThreads Number of threads
     * @param maxRetryCount Max retry count. set this to -1 for no retry restrictions
     */
    protected FlatTileResourceFetcher(int nThreads, int maxRetryCount) {
        super(nThreads, maxRetryCount);
    }

    @Override
    protected Integer processorKeyToQueueKey(FlatTileKey processorKey) {
        return processorKey.relativeZoom;
    }

    @Override
    protected ByteBuf processInternal(FlatTileKey key, String s) throws Exception {
        return HttpResourceManager.download(s);
    }
}
