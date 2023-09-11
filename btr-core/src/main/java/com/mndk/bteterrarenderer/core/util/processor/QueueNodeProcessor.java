package com.mndk.bteterrarenderer.core.util.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public abstract class QueueNodeProcessor<T> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG = false;
    private static void log(String message) {
        if(DEBUG) LOGGER.info(message);
    }

    private final long nodeExpireTime;
    private final int maxRetryCount;

    private final Map<T, Integer> failedCountMap = new HashMap<>();
    protected QueueNode currentQueueNode;
    private int queueCount = 1;

    protected QueueNodeProcessor(long nodeExpireTime, int maxRetryCount) {
        this.currentQueueNode = new QueueNode();
        this.nodeExpireTime = nodeExpireTime;
        this.maxRetryCount = maxRetryCount;
    }

    public synchronized boolean isEmpty() {
        return currentQueueNode.isEmpty() && currentQueueNode.previous == null;
    }

    protected synchronized void offerToQueue(T element) {
        this.updateQueueNode();
        currentQueueNode.offer(element);
    }

    public synchronized void newQueue() {
        long now = System.currentTimeMillis();
        // You don't need to make a new queue node if the current one is empty (+not expired)
        if(!currentQueueNode.isExpired(now) && !currentQueueNode.isEmpty()) {
            QueueNode newQueue = new QueueNode();
            newQueue.previous = this.currentQueueNode;
            this.currentQueueNode = newQueue;
            log("Starting new queue (Queue count: " + ++queueCount + ")");
        }
    }

    public synchronized final void process(int updateAtATime) {
        this.currentQueueNode.update(updateAtATime);
        this.updateQueueNode();
    }

    protected synchronized void updateQueueNode() {
        long now = System.currentTimeMillis();
        while(currentQueueNode.isExpired(now)) {
            currentQueueNode = currentQueueNode.previous;
            log("Queue expired (Queue count: " + --queueCount + ")");
        }
    }

    /**
     * @param element The element. It's always non-null since the processor checks it first before
     *                calling this command
     * @return {@code true} if the processing was a success. {@code false} otherwise
     */
    protected abstract boolean processQueueElement(T element);

    /**
     * This method is called when the processor has failed to process the same element more than enough
     * @param element The element
     */
    protected abstract void onQueueElementProcessingFailed(T element);

    protected class QueueNode {
        private final Queue<T> queue = new ArrayDeque<>();
        private QueueNode previous = null;
        private long timeSinceEmptyMilliseconds = -1;

        private synchronized void offer(T element) {
            this.queue.offer(element);
            this.timeSinceEmptyMilliseconds = -1;
        }

        /**
         * @param now The current time
         * @return {@code true} If the node is expired, and its previous node isn't {@code null}
         *         (Since the first node should never expire).
         */
        private synchronized boolean isExpired(long now) {
            if(previous == null) return false;
            return this.timeSinceEmptyMilliseconds != -1 && now - timeSinceEmptyMilliseconds >= nodeExpireTime;
        }

        private synchronized void update(int updateAtATime) {
            List<T> fails = new ArrayList<>();
            for (int i = 0; i < updateAtATime && !queue.isEmpty(); i++) {
                T element = queue.poll();
                if (element == null) continue;

                boolean success = processQueueElement(element);
                if(!success) {
                    // Skip the element if the failed count is more than enough
                    if(maxRetryCount != -1) {
                        if (!failedCountMap.containsKey(element)) {
                            int previousRetryCount = failedCountMap.get(element);
                            if (previousRetryCount >= maxRetryCount) {
                                onQueueElementProcessingFailed(element);
                                continue;
                            }

                            failedCountMap.put(element, previousRetryCount + 1);
                        } else {
                            failedCountMap.put(element, 1);
                        }
                    }
                    fails.add(element);
                } else {
                    failedCountMap.remove(element);
                }
            }
            this.queue.addAll(fails);

            if(queue.isEmpty() && this.timeSinceEmptyMilliseconds == -1) {
                this.timeSinceEmptyMilliseconds = System.currentTimeMillis();
            }
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
