package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.util.queue.QueueNodeProcessor;
import lombok.RequiredArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileImageDownloader extends QueueNodeProcessor<TileImageDownloader.DownloadRequest> {

    public static final int DOWNLOAD_RETRY_COUNT = 3;

    private final ExecutorService executorService;

    protected TileImageDownloader(int nThreads) {
        super(1000 /* 1 second */);
        this.executorService = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    protected void processQueue(Queue<DownloadRequest> queue) {

    }

    @RequiredArgsConstructor
    public static class DownloadRequest {
        private final Object key;
        private final String url;
        private final int retry;

    }
}
