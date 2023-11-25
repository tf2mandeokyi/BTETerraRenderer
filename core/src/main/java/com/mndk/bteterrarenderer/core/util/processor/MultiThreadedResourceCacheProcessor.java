package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.util.Loggers;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
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
     * @param maxRetryCount      Max retry count. set this to -1 if no retry restrictions are needed
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
    protected void offerToProcessor(Key key, Input input) {
        executorService.execute(new ResourceProcessingTask(key, input, 0, null));
    }

    @RequiredArgsConstructor
    private class ResourceProcessingTask implements Runnable {

        private final Key key;
        private final Input input;
        private final int retry;
        @Nullable
        private final Exception exception;
        private final MultiThreadedResourceCacheProcessor<Key, Input, Resource> processor =
                MultiThreadedResourceCacheProcessor.this;

        @Override
        public void run() {
            if(processor.maxRetryCount != -1 && retry >= processor.maxRetryCount) {
                processor.resourcePreparingError(key, exception);
                return;
            }

            Exception newException;
            try {
                processor.processResource(key, input);
                return;
            } catch(Exception e) {
                Loggers.get(this).error("Caught exception while processing a resource (" +
                        "Key=" + key + ", Retry #" + (retry + 1) + ")", e);
                newException = e;
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    executorService.execute(new ResourceProcessingTask(key, input, retry + 1, newException));
                }
            }, retryDelayMilliseconds);
        }
    }
}
