package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.util.processor.block.MappedQueueBlock;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;

public class FlatTileResourceDownloadingBlock extends MappedQueueBlock<FlatTileKey, Integer, String, ByteBuf> {

    /**
     * @param nThreads Number of threads
     * @param maxRetryCount Max retry count. set this to -1 for no retry restrictions
     */
    protected FlatTileResourceDownloadingBlock(int nThreads, int maxRetryCount, boolean closeableByModel,
                                               int initialRelativeZoom) {
        super(nThreads, maxRetryCount, closeableByModel, initialRelativeZoom);
    }

    @Override
    protected Integer processorKeyToQueueKey(FlatTileKey key) {
        return key.relativeZoom;
    }

    @Override
    protected ByteBuf processInternal(FlatTileKey key, @Nonnull String s) throws Exception {
        return HttpResourceManager.download(s);
    }
}
