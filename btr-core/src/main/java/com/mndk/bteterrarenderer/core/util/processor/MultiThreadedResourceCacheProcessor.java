package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.RequiredArgsConstructor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public abstract class MultiThreadedResourceCacheProcessor<Key, Input, Resource>
        extends AbstractResourceCacheProcessor<Key, Input, Resource> {

    private static final Timer TIMER = new Timer();

    private final ExecutorService executorService;
    private final int maxRetryCount;
    private final int retryDelayMilliseconds;

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed
     * @param maximumSize        Maximum cache size
     * @param debug              debug
     */
    protected MultiThreadedResourceCacheProcessor(ExecutorService executorService,
                                                  long expireMilliseconds, int maximumSize,
                                                  int maxRetryCount, int retryDelayMilliseconds,
                                                  boolean debug) {
        super(expireMilliseconds, maximumSize, debug);
        this.executorService = executorService;
        this.maxRetryCount = maxRetryCount;
        this.retryDelayMilliseconds = retryDelayMilliseconds;
    }

    @Override
    protected void updateProcessor() {}

    @Override
    protected void offerToProcessor(Key key, Input input) {
        executorService.execute(new ResourceProcessingTask(key, input, 0));
    }

    @RequiredArgsConstructor
    private class ResourceProcessingTask implements Runnable {

        private final Key key;
        private final Input input;
        private final int retry;
        private final MultiThreadedResourceCacheProcessor<Key, Input, Resource> processor =
                MultiThreadedResourceCacheProcessor.this;

        @Override
        public void run() {
            if(retry >= processor.maxRetryCount) {
                processor.resourcePreparingError(key);
            }

            try {
                processor.processResource(key, input);
                return;
            } catch(Exception e) {
                BTETerraRendererConstants.LOGGER.error("Caught exception while processing a resource (" +
                        "Key=" + key + ", Retry #" + (retry + 1) + ")", e);
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    executorService.execute(new ResourceProcessingTask(key, input, retry + 1));
                }
            }, retryDelayMilliseconds);
        }
    }
}
