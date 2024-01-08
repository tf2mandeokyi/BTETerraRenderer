package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class SimpleImageFetchingBlock<Key> extends MultiThreadedBlock<Key, URL, BufferedImage> {

    /**
     * @param maxRetryCount          Max retry count. set this to -1 if no retry restrictions are needed
     */
    public SimpleImageFetchingBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds,
                                    boolean closeableByModel) {
        super(executorService, maxRetryCount, retryDelayMilliseconds, closeableByModel);
    }

    @Override
    protected BufferedImage processInternal(Key key, @Nonnull URL url) throws Exception {
        return HttpResourceManager.downloadAsImage(url.toString());
    }
}
