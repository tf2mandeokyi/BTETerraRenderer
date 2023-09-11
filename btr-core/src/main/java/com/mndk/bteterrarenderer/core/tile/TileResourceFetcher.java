package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.core.util.processor.MultiThreadedResourceCacheProcessor;
import com.mndk.bteterrarenderer.dep.terraplusplus.HttpResourceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class TileResourceFetcher<TileId> extends MultiThreadedResourceCacheProcessor<TileId, URL, ByteBuf> {

    protected TileResourceFetcher(ExecutorService executorService) {
        super(executorService, 1000 * 60 * 5 /* 5 minutes */, 10000, 3, 1000, false);
    }

    @Override
    protected ByteBuf processResource(URL url) throws IOException {
        InputStream stream = HttpResourceManager.download(url.toString());
        ByteBuf buf = Unpooled.copiedBuffer(IOUtil.readAllBytes(stream));
        stream.close();
        return buf;
    }

    @Override
    protected void deleteResource(ByteBuf byteBuf) {}

}
