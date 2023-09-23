package com.mndk.bteterrarenderer.core.util.processor;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public abstract class SimpleResourceCacheProcessor<Key, Input, Resource>
        extends AbstractResourceCacheProcessor<Key, Input, Resource> {

    private final Queue<Map.Entry<Key, Input>> queue = new ArrayDeque<>();

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed
     * @param maximumSize        Maximum cache size
     * @param debug              debug
     */
    protected SimpleResourceCacheProcessor(long expireMilliseconds, int maximumSize, boolean debug) {
        super(expireMilliseconds, maximumSize, debug);
    }

    @Override
    protected void offerToProcessor(Key key, Input input) {
        queue.add(new AbstractMap.SimpleEntry<>(key, input));
    }

    public void process(int processAtATime) {
        for(int i = 0; i < processAtATime && !queue.isEmpty(); i++) {
            Map.Entry<Key, Input> entry = queue.poll();
            if (entry == null) continue;

            try {
                this.processResource(entry.getKey(), entry.getValue());
            } catch(Exception e) {
                BTETerraRendererConstants.LOGGER.error("Caught exception while processing a resource (" +
                        "Key=" + entry.getKey() + ")", e);
            }
        }
    }
}
