package com.mndk.bteterrarenderer.core.util.processor;

import java.util.*;

public abstract class MappedQueueProcessor<QueueKey, Input> {

    private final int maxRetryCount;
    private final Map<Input, Integer> failedCountMap = new HashMap<>();
    private final Map<QueueKey, Queue<Input>> queueMaps = new HashMap<>();
    private QueueKey currentQueueKey = null;

    protected MappedQueueProcessor(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public synchronized void setCurrentQueueKey(QueueKey queueKey) {
        this.currentQueueKey = queueKey;
    }

    public synchronized boolean isCurrentQueueEmpty() {
        return queueMaps.isEmpty();
    }

    protected synchronized void offerToQueue(QueueKey queueKey, Input element) {
        Queue<Input> queue = queueMaps.computeIfAbsent(queueKey, key -> new ArrayDeque<>());
        queue.offer(element);
    }

    public final void process(int processAtATime) {
        List<Input> todos = new ArrayList<>();
        Queue<Input> queue;

        synchronized(this) {
            if(this.currentQueueKey == null) return;
            queue = queueMaps.computeIfAbsent(this.currentQueueKey, key -> new ArrayDeque<>());
            for (int i = 0; i < processAtATime && !queue.isEmpty(); i++) {
                Input element = queue.poll();
                if (element == null) continue;
                todos.add(element);
            }
        }

        List<Input> fails = new ArrayList<>();
        for (Input todo : todos) {
            Exception exception = processQueueElement(todo);
            if (exception == null) {
                failedCountMap.remove(todo);
            } else {
                // Skip the element if the failed count is more than enough
                if(maxRetryCount != -1) {
                    int previousRetryCount = failedCountMap.computeIfAbsent(todo, key -> 0);
                    if (previousRetryCount >= maxRetryCount) {
                        onQueueElementProcessingFailed(todo, exception);
                        continue;
                    }

                    failedCountMap.put(todo, previousRetryCount + 1);
                }
                fails.add(todo);
            }
        }

        if(!fails.isEmpty()) synchronized(this) {
            queue.addAll(fails);
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
    protected abstract void onQueueElementProcessingFailed(Input element, Exception exception);
}
