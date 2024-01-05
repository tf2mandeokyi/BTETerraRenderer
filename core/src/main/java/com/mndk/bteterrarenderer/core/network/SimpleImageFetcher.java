package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.core.util.processor.block.MultiThreadedBlock;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class SimpleImageFetcher<Key> extends MultiThreadedBlock<Key, URL, BufferedImage> {

    private final int paletteWidth, paletteHeight;
    private final double paletteRatio;

    /**
     * @param maxRetryCount          Max retry count. set this to -1 if no retry restrictions are needed
     */
    public SimpleImageFetcher(ExecutorService executorService,
                              int maxRetryCount, int retryDelayMilliseconds,
                              int paletteWidth, int paletteHeight) {
        super(executorService, maxRetryCount, retryDelayMilliseconds);
        this.paletteWidth = paletteWidth;
        this.paletteHeight = paletteHeight;
        this.paletteRatio = (double) paletteHeight / paletteWidth;
    }

    @Override
    protected BufferedImage processInternal(Key key, URL url) throws Exception {
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
}
