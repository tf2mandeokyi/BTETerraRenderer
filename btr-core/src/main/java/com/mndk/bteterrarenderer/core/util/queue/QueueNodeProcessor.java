package com.mndk.bteterrarenderer.core.util.queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class QueueNodeProcessor<T> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean DEBUG = false;
    private static void log(String message) {
        if(DEBUG) LOGGER.info(message);
    }

    protected QueueNode currentQueueNode;
    private int queueCount = 1;
    private final long nodeExpireTime;

    protected QueueNodeProcessor(long nodeExpireTime) {
        this.currentQueueNode = new QueueNode();
        this.nodeExpireTime = nodeExpireTime;
    }

    protected synchronized void offerQueue(T element) {
        this.updateQueueNode();
        currentQueueNode.offer(element);
    }

    public synchronized void newQueue() {
        long now = System.currentTimeMillis();
        if(!currentQueueNode.isExpired(now) && !currentQueueNode.isEmpty()) {
            QueueNode newQueue = new QueueNode();
            newQueue.previous = this.currentQueueNode;
            this.currentQueueNode = newQueue;
            log("Starting new queue (Queue count: " + ++queueCount + ")");
        }
    }

    public synchronized final void process() {
        this.currentQueueNode.update();
        this.updateQueueNode();
    }

    protected synchronized void updateQueueNode() {
        long now = System.currentTimeMillis();
        while(currentQueueNode.isExpired(now)) {
            currentQueueNode = currentQueueNode.previous;
            log("Queue expired (Queue count: " + --queueCount + ")");
        }
    }

    protected abstract void processQueue(Queue<T> queue);

    protected class QueueNode {
        private final Queue<T> queue = new ArrayDeque<>();
        private QueueNode previous = null;
        private long timeSinceEmptyMilliseconds = -1;

        private synchronized void offer(T element) {
            this.queue.offer(element);
            this.timeSinceEmptyMilliseconds = -1;
        }

        private synchronized boolean isExpired(long now) {
            if(previous == null) return false;
            return this.timeSinceEmptyMilliseconds != -1 && now - timeSinceEmptyMilliseconds >= nodeExpireTime;
        }

        private synchronized void update() {
            processQueue(this.queue);
            if(queue.isEmpty() && this.timeSinceEmptyMilliseconds == -1) {
                this.timeSinceEmptyMilliseconds = System.currentTimeMillis();
            }
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
