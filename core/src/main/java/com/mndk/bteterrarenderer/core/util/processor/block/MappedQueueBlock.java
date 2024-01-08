package com.mndk.bteterrarenderer.core.util.processor.block;

import com.mndk.bteterrarenderer.core.util.Loggers;
import com.mndk.bteterrarenderer.core.util.processor.BlockPayload;
import com.mndk.bteterrarenderer.core.util.processor.MappedQueueProcessor;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;

public abstract class MappedQueueBlock<Key, QueueKey, Input, Output> extends ProcessingBlock<Key, Input, Output>
        implements Closeable {

    private final SimpleMappedQueueProcessor processor;
    private final Thread[] threads;

    /**
     * @param maxRetryCount Max retry count. set this to -1 for no retry restrictions
     */
    protected MappedQueueBlock(int nThreads, int maxRetryCount, boolean closeableByModel) {
        super(closeableByModel);
        this.processor = new SimpleMappedQueueProcessor(maxRetryCount);
        this.threads = new Thread[nThreads];
        for(int i = 0; i < nThreads; i++) {
            Thread t = new Thread(new CurrentQueueWatchingTask(i));
            this.threads[i] = t;
            t.start();
        }
    }

    public void setQueueKey(QueueKey queueKey) {
        this.processor.setCurrentQueueKey(queueKey);
    }

    @Override
    public void insert(BlockPayload<Key, Input> payload) {
        QueueKey queueKey = this.processorKeyToQueueKey(payload.getKey());
        this.processor.offerToQueue(queueKey, payload);
    }

    @Override
    public void close() {
        for(Thread t : this.threads) {
            if(t.isAlive()) t.interrupt();
        }
    }

    protected abstract QueueKey processorKeyToQueueKey(Key processorKey);

    private class SimpleMappedQueueProcessor extends MappedQueueProcessor<QueueKey, BlockPayload<Key, Input>> {

        protected SimpleMappedQueueProcessor(int maxRetryCount) {
            super(maxRetryCount);
        }

        @Override
        protected Exception processInput(BlockPayload<Key, Input> payload) {
            try {
                MappedQueueBlock.this.process(payload);
                return null;
            } catch (Exception e) {
                Loggers.get(this).error("Error while processing a cache", e);
                // Put the input back to the queue if something went wrong
                return e;
            }
        }

        @Override
        protected void onInputProcessingFail(BlockPayload<Key, Input> input, Exception exception) {
            MappedQueueBlock.this.onProcessingFail(input, exception);
        }
    }

    @RequiredArgsConstructor
    private class CurrentQueueWatchingTask implements Runnable {
        private final int index;

        @SuppressWarnings({ "InfiniteLoopStatement", "BusyWait" })
        public void run() {
            if(debug) Loggers.get(this).info("Thread #" + index + " started");
            try {
                while(true) {
                    if (processor.isCurrentQueueEmpty()) {
                        Thread.sleep(250);
                        continue;
                    }
                    processor.processOne();
                }
            } catch (InterruptedException ignored) {}
        }
    }
}
