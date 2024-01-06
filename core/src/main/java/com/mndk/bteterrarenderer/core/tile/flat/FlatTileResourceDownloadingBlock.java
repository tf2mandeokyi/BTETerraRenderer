package com.mndk.bteterrarenderer.core.tile.flat;

import com.mndk.bteterrarenderer.core.network.HttpResourceManager;
import com.mndk.bteterrarenderer.core.tile.TMSIdPair;
import com.mndk.bteterrarenderer.core.util.processor.block.MappedQueueBlock;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;

public class FlatTileResourceDownloadingBlock extends MappedQueueBlock<TMSIdPair<FlatTileKey>, Integer, String, ByteBuf> {

    /**
     * @param nThreads Number of threads
     * @param maxRetryCount Max retry count. set this to -1 for no retry restrictions
     */
    protected FlatTileResourceDownloadingBlock(int nThreads, int maxRetryCount) {
        super(nThreads, maxRetryCount);
    }

    @Override
    protected Integer processorKeyToQueueKey(TMSIdPair<FlatTileKey> key) {
        return key.getRight().relativeZoom;
    }

    @Override
    protected ByteBuf processInternal(TMSIdPair<FlatTileKey> key, @Nonnull String s) throws Exception {
        return HttpResourceManager.download(s);
    }
}
