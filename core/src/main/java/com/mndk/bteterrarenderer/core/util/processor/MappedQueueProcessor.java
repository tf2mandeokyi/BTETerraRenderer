package com.mndk.bteterrarenderer.core.util.processor;

import lombok.Setter;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class MappedQueueProcessor<QueueKey, Input> {

    private final int maxRetryCount;
    private final Map<Input, Integer> failedCountMap = new ConcurrentHashMap<>();
    private final Map<QueueKey, Queue<Input>> queueMaps = new ConcurrentHashMap<>();

    @Setter
    private QueueKey currentQueueKey;

    protected MappedQueueProcessor(int maxRetryCount, QueueKey initialQueueKey) {
        this.maxRetryCount = maxRetryCount;
        this.currentQueueKey = initialQueueKey;
    }

    private synchronized Queue<Input> getQueue(QueueKey queueKey) {
        return queueMaps.computeIfAbsent(queueKey, key -> new ConcurrentLinkedDeque<>());
    }

    public synchronized boolean isCurrentQueueEmpty() {
        return this.getQueue(this.currentQueueKey).isEmpty();
    }

    public synchronized void offerToQueue(QueueKey queueKey, Input input) {
        Queue<Input> queue = this.getQueue(queueKey);
        queue.offer(input);
    }

    public final void processOne() throws InterruptedException {
        Input input;
        Queue<Input> queue;

        synchronized(this) {
            if (this.currentQueueKey == null) return;
            queue = this.getQueue(this.currentQueueKey);
            input = queue.poll();
            if (input == null) return;
        }

        // Should not be synchronized with anything
        Exception exception = processInput(input);

        synchronized(this) {
            if (exception == null) {
                failedCountMap.remove(input);
            } else {
                // Skip the input if the failed count is more than enough
                if (maxRetryCount != -1) {
                    int previousRetryCount = failedCountMap.computeIfAbsent(input, key -> 0);
                    if (previousRetryCount >= maxRetryCount) {
                        onInputProcessingFail(input, exception);
                        return;
                    }
                    failedCountMap.put(input, previousRetryCount + 1);
                }
                queue.add(input);
            }
        }
    }

    /**
     * @param input The input. It's always non-null since the processor checks whether it's null before
     *              calling this method
     * @return {@code null} if the processing was a success. otherwise the exception object
     */
    protected abstract Exception processInput(Input input);

    /**
     * This method is called when the processor has failed to process the same input more than enough
     * @param input     The input
     * @param exception The exception
     */
    protected abstract void onInputProcessingFail(Input input, Exception exception);
}
