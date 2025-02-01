package com.mndk.bteterrarenderer.core.util.concurrent;

import com.mndk.bteterrarenderer.util.Loggers;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class MappedExecutors<Key> {

    private final Map<Key, QueueWrapper> map = new ConcurrentHashMap<>();
    private final Executor executor;
    private volatile Key currentKey;

    public MappedExecutors(Executor executor, Key currentKey) {
        this.executor = executor;
        this.currentKey = currentKey;
    }

    public void setCurrentKey(Key key) {
        currentKey = key;
        QueueWrapper qw = map.get(key);
        if (qw != null && !qw.queue.isEmpty() && qw.drainRunning.compareAndSet(false, true)) {
            executor.execute(() -> drain(key, qw));
        }
    }

    public Executor getExecutor(Key key) {
        QueueWrapper qw = map.computeIfAbsent(key, k -> new QueueWrapper());
        return r -> {
            qw.queue.addLast(r);
            if (key.equals(currentKey) && qw.drainRunning.compareAndSet(false, true)) {
                executor.execute(() -> drain(key, qw));
            }
        };
    }

    private void drain(Key key, QueueWrapper qw) {
        try {
            while (true) {
                if (!key.equals(currentKey)) break;

                Runnable task = qw.queue.pollFirst();
                if (task == null) break;

                if (!key.equals(currentKey)) {
                    qw.queue.addFirst(task);
                    break;
                }
                try { task.run(); }
                catch (RuntimeException e) { Loggers.get(this).error(e); }
            }
        } finally {
            qw.drainRunning.set(false);
            if (key.equals(currentKey) && !qw.queue.isEmpty() && qw.drainRunning.compareAndSet(false, true)) {
                executor.execute(() -> drain(key, qw));
            }
        }
    }

    private static class QueueWrapper {
        final Deque<Runnable> queue = new ConcurrentLinkedDeque<>();
        final AtomicBoolean drainRunning = new AtomicBoolean(false);
    }
}
