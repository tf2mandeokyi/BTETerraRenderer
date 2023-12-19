package com.mndk.bteterrarenderer.dep.terraplusplus;

import com.mndk.bteterrarenderer.dep.terraplusplus.http.Http;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@UtilityClass
public class HttpResourceManager {

    private final NullPointerException NPE = new NullPointerException();

    public BufferedImage downloadAsImage(String url) throws ExecutionException, InterruptedException, IOException {
        ByteBuf buf = download(url);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        // Try bitmap type image
        try {
            InputStream stream = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(stream);
            stream.close();

            if(image == null) throw NPE;
            return image;
        } catch(IOException | NullPointerException ignored) {}

        throw new IOException("Cannot specify image type for " + url);
    }

    public ByteBuf download(String url) throws ExecutionException, InterruptedException {
        return Http.get(url).get();
    }
}
