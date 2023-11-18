package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.util.processor.MultiThreadedResourceCacheProcessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class SimpleImageFetcher extends MultiThreadedResourceCacheProcessor<URL, URL, BufferedImage> {

    /**
     * @param expireMilliseconds     How long can a cache live without being refreshed
     * @param maximumSize            Maximum cache size
     * @param maxRetryCount          Max retry count. set this to -1 if no retry restrictions are needed
     * @param debug                  debug
     */
    public SimpleImageFetcher(ExecutorService executorService,
                                 long expireMilliseconds, int maximumSize,
                                 int maxRetryCount, int retryDelayMilliseconds,
                                 boolean debug) {
        super(executorService, expireMilliseconds, maximumSize, maxRetryCount, retryDelayMilliseconds, debug);
    }

    @Override
    protected BufferedImage processResource(URL url) throws Exception {
        // Download
        InputStream stream = HttpResourceManager.download(url.toString());
        BufferedImage image = ImageIO.read(stream);
        stream.close();
        return image;
    }

    @Override
    protected void deleteResource(BufferedImage image) {}
}
