package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.util.processor.MultiThreadedResourceCacheProcessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class SimpleImageFetcher extends MultiThreadedResourceCacheProcessor<URL, URL, BufferedImage> {

    private final int paletteWidth, paletteHeight;
    private final double paletteRatio;

    /**
     * @param expireMilliseconds     How long can a cache live without being refreshed
     * @param maximumSize            Maximum cache size
     * @param maxRetryCount          Max retry count. set this to -1 if no retry restrictions are needed
     * @param debug                  debug
     */
    public SimpleImageFetcher(ExecutorService executorService,
                              long expireMilliseconds, int maximumSize,
                              int maxRetryCount, int retryDelayMilliseconds,
                              int paletteWidth, int paletteHeight,
                              boolean debug) {
        super(executorService, expireMilliseconds, maximumSize, maxRetryCount, retryDelayMilliseconds, debug);
        this.paletteWidth = paletteWidth;
        this.paletteHeight = paletteHeight;
        this.paletteRatio = (double) paletteHeight / paletteWidth;
    }

    @Override
    protected BufferedImage processResource(URL url) throws Exception {
        BufferedImage image = HttpResourceManager.downloadAsImage(url.toString());
        if(image == null) throw new NullPointerException("Image is null");
        if(this.paletteWidth <= 0 || this.paletteHeight <= 0) return image;

        BufferedImage palette = new BufferedImage(this.paletteWidth, this.paletteHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = palette.createGraphics();
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, this.paletteWidth, this.paletteHeight);

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        double imageRatio = (double) imageHeight / imageWidth;
        if(this.paletteRatio > imageRatio) {
            int centerY = this.paletteHeight / 2, height = (int) (this.paletteWidth * imageRatio);
            g2d.drawImage(image, 0, centerY - height / 2, this.paletteWidth, height, null);
        } else {
            int centerX = this.paletteWidth / 2, width = (int) (this.paletteHeight / imageRatio);
            g2d.drawImage(image, centerX - width / 2, 0, width, this.paletteHeight, null);
        }

        g2d.dispose();
        return palette;
    }

    @Override
    protected void deleteResource(BufferedImage image) {}
}
