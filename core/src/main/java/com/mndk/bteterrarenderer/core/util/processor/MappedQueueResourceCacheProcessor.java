package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.RequiredArgsConstructor;

/**
 * Does something {@link java.util.concurrent.Executors#newFixedThreadPool(int)} can't do
 */
public abstract class MappedQueueResourceCacheProcessor<Key, QueueKey, Input, Resource>
        extends AbstractResourceCacheProcessor<Key, Input, Resource>
        implements AutoCloseable {

    private final SimpleMappedQueueProcessor queueNodeProcessor;
    private final Thread[] threads;
    private final int nThreads;
    private final Thread threadsWatcher;

    /**
     * @param maxRetryCount           Max retry count. set this to -1 if no retry restrictions are needed
     * @param cacheExpireMilliseconds How long can a cache live without being refreshed
     * @param maximumSize             Maximum cache size
     * @param debug                   debug
     */
    protected MappedQueueResourceCacheProcessor(int nThreads, int maxRetryCount,
                                                long cacheExpireMilliseconds, int maximumSize, boolean debug) {
        super(cacheExpireMilliseconds, maximumSize, debug);
        this.queueNodeProcessor = new SimpleMappedQueueProcessor(maxRetryCount);
        this.threads = new Thread[nThreads];
        this.nThreads = nThreads;
        this.threadsWatcher = new Thread(new ThreadsWatcherTask());
        this.threadsWatcher.start();
    }

    public void setCurrentQueueKey(QueueKey queueKey) {
        this.queueNodeProcessor.setCurrentQueueKey(queueKey);
    }

    @Override
    protected void offerToProcessor(Key key, Input input) {
        this.queueNodeProcessor.offerToQueue(this.keyToQueueKey(key), new CacheProcessRequest<>(key, input));
    }

    @Override
    public void close() {
        for(Thread t : this.threads) {
            if(t != null && t.isAlive()) t.interrupt();
        }
        this.threadsWatcher.interrupt();
    }

    protected abstract QueueKey keyToQueueKey(Key key);

    private class SimpleMappedQueueProcessor extends MappedQueueProcessor<QueueKey, CacheProcessRequest<Key, Input>> {

        protected SimpleMappedQueueProcessor(int maxRetryCount) {
            super(maxRetryCount);
        }

        @Override
        protected Exception processQueueElement(CacheProcessRequest<Key, Input> element) {
            try {
                processResource(element.key, element.input);
                return null;
            } catch (Exception e) {
                BTETerraRendererConstants.LOGGER.error("Error while processing a cache", e);
                // Put the input back to the queue if something went wrong
                return e;
            }
        }

        @Override
        protected void onQueueElementProcessingFailed(CacheProcessRequest<Key, Input> element, Exception exception) {
            resourcePreparingError(element.key, exception);
        }
    }

    @RequiredArgsConstructor
    private class SingleThreadTask implements Runnable {
        private final int index;
        public void run() {
            if(debug) BTETerraRendererConstants.LOGGER.info("Thread #" + index + " started");
            if(!queueNodeProcessor.isCurrentQueueEmpty()) queueNodeProcessor.process(1);
            threads[index] = null;
        }
    }

    private class ThreadsWatcherTask implements Runnable {
        @Override
        public void run() {
            while(true) {
                if(queueNodeProcessor.isCurrentQueueEmpty()) {
                    try {
                        Thread.sleep(250);
                        continue;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                for(int i = 0; i < nThreads; i++) {
                    if(threads[i] != null) continue;
                    threads[i] = new Thread(new SingleThreadTask(i));
                    threads[i].start();
                }
            }
        }
    }
}
