package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public abstract class MultiThreadedBlock<Key, Input, Output> extends ProcessingBlock<Key, Input, Output> {

    private static final Timer TIMER = new Timer();

    private final ExecutorService executorService;
    private final int maxRetryCount;
    private final int retryDelayMilliseconds;

    protected MultiThreadedBlock(ExecutorService executorService, int maxRetryCount, int retryDelayMilliseconds) {
        this.executorService = executorService;
        this.maxRetryCount = maxRetryCount;
        this.retryDelayMilliseconds = retryDelayMilliseconds;
    }

    @Override
    public void insert(BlockPayload<Key, Input> payload) {
        executorService.execute(new ResourceProcessingTask(payload, 0, null));
    }

    public static <K, I, O> MultiThreadedBlock<K, I, O> of(BlockFunction<K, I, O> function,
                                                           ExecutorService executorService,
                                                           int maxRetryCount, int retryDelayMilliseconds) {
        return new MultiThreadedBlock<K, I, O>(executorService, maxRetryCount, retryDelayMilliseconds) {
            protected O processInternal(K key, @Nonnull I input) throws Exception {
                return function.apply(key, input);
            }
        };
    }

    @RequiredArgsConstructor
    private class ResourceProcessingTask implements Runnable {
        private final BlockPayload<Key, Input> payload;
        private final int retry;
        @Nullable
        private final Exception error;

        @Override
        public void run() {
            if(maxRetryCount != -1 && retry >= maxRetryCount) {
                MultiThreadedBlock.this.onProcessingFail(payload, error);
                return;
            }

            Exception newError;
            try {
                MultiThreadedBlock.this.process(payload);
                return;
            } catch(Exception e) {
                Loggers.get(this).error("Caught exception while processing a resource (" +
                        "Key=" + payload.getKey() + ", Retry #" + (retry + 1) + ")", e);
                newError = e;
            }

            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    executorService.execute(new ResourceProcessingTask(payload, retry + 1, newError));
                }
            }, retryDelayMilliseconds);
        }
    }
}
