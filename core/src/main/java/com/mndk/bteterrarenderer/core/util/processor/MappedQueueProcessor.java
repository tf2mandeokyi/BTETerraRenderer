package com.mndk.bteterrarenderer.core.util.processor;

import lombok.Setter;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public abstract class MappedQueueProcessor<QueueKey, Input> {

    private final int maxRetryCount;
    private final Map<Input, Integer> failedCountMap = new HashMap<>();
    private final Map<QueueKey, Queue<Input>> queueMaps = new HashMap<>();

    @Setter
    private QueueKey currentQueueKey = null;

    protected MappedQueueProcessor(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    private synchronized Queue<Input> getQueue(QueueKey queueKey) {
        return queueMaps.computeIfAbsent(queueKey, key -> new ArrayDeque<>());
    }

    public boolean isCurrentQueueEmpty() {
        return this.getQueue(this.currentQueueKey).isEmpty();
    }

    public void offerToQueue(QueueKey queueKey, Input element) {
        Queue<Input> queue = this.getQueue(queueKey);
        queue.offer(element);
    }

    public final void processOne() throws InterruptedException {
        Input element;

        if(this.currentQueueKey == null) return;
        Queue<Input> queue = this.getQueue(this.currentQueueKey);
        element = queue.poll();
        if(element == null) return;

        Exception exception = processQueueElement(element);
        if (exception == null) {
            failedCountMap.remove(element);
        } else {
            // Skip the element if the failed count is more than enough
            if(maxRetryCount != -1) {
                int previousRetryCount = failedCountMap.computeIfAbsent(element, key -> 0);
                if (previousRetryCount >= maxRetryCount) {
                    onQueueElementProcessingFail(element, exception);
                    return;
                }

                failedCountMap.put(element, previousRetryCount + 1);
            }
            queue.add(element);
        }
    }

    /**
     * @param element The element. It's always non-null since the processor checks whether it's null before
     *                calling this method
     * @return {@code null} if the processing was a success. otherwise the exception object
     */
    protected abstract Exception processQueueElement(Input element);

    /**
     * This method is called when the processor has failed to process the same element more than enough
     *
     * @param element   The element
     * @param exception The exception
     */
    protected abstract void onQueueElementProcessingFail(Input element, Exception exception);
}
