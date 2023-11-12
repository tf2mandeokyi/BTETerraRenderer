package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.processor.MultiThreadedResourceCacheProcessor;
import com.mndk.bteterrarenderer.ogc3dtiles.TileResourceManager;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedTileParser<Key> extends MultiThreadedResourceCacheProcessor<Key, PreParsedData, ParsedData> {

    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();
    private static final CachedTileParser<?> INSTANCE = new CachedTileParser<>(SERVICE);
    public static <T> CachedTileParser<T> getInstance() {
        return BTRUtil.uncheckedCast(INSTANCE);
    }


    private CachedTileParser(ExecutorService executorService) {
        super(executorService, 1000 * 60 * 10 /* 10 minutes */, 10000, -1, 100, false);
    }

    @Override
    protected ParsedData processResource(PreParsedData preParsedData) throws Exception {
        Matrix4 transform = preParsedData.getTransform();
        InputStream stream = preParsedData.getStream();
        return new ParsedData(transform, TileResourceManager.parse(stream));
    }

    @Override
    protected void deleteResource(ParsedData tileData) {}
}
