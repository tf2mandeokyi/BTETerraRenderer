package com.mndk.bteterrarenderer.core.util.processor;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractResourceCacheProcessor<Key, Input, Resource> {

    private static final Logger LOGGER = LogManager.getLogger();
    private void log(String message) {
        if(this.debug) LOGGER.info(message);
    }

    private final int maximumSize;
    private final boolean debug;
    private final long expireMilliseconds;

    private final Map<Key, ProcessedPayloadWrapper<Resource>> resourceWrapperMap = new HashMap<>();

    /**
     * @param expireMilliseconds How long can a cache live without being refreshed
     * @param maximumSize Maximum cache size
     * @param debug debug
     */
    protected AbstractResourceCacheProcessor(long expireMilliseconds, int maximumSize, boolean debug) {
        this.expireMilliseconds = expireMilliseconds;
        this.maximumSize = maximumSize;
        this.debug = debug;
    }

    @Nonnull
    public ProcessingState getResourceProcessingState(Key key) {
        ProcessedPayloadWrapper<Resource> wrapper;
        synchronized(resourceWrapperMap) { wrapper = resourceWrapperMap.get(key); }
        if(wrapper != null) return wrapper.state;
        return ProcessingState.NOT_PROCESSED;
    }

    private void setResourceState(Key key, ProcessingState state) {
        ProcessedPayloadWrapper<Resource> wrapper;
        synchronized(resourceWrapperMap) { wrapper = resourceWrapperMap.get(key); }
        if(wrapper != null) {
            wrapper.state = state;
        } else synchronized(resourceWrapperMap) {
            resourceWrapperMap.put(key, new ProcessedPayloadWrapper<>(state, null, -1));
        }
    }

    public void setResourceInPreparingState(Key key) {
        this.setResourceState(key, ProcessingState.PREPARING);
    }

    public void resourcePreparingError(Key key) {
        this.setResourceState(key, ProcessingState.ERROR);
    }

    public void resourceProcessingReady(Key key, Input input) {
        this.setResourceState(key, ProcessingState.PROCESSING);
        this.offerToProcessor(key, input);
    }

    protected void processResource(Key key, Input input) throws Exception {
        ProcessedPayloadWrapper<Resource> wrapper;
        synchronized(resourceWrapperMap) {
            if (this.maximumSize != -1 && resourceWrapperMap.size() >= this.maximumSize) {
                this.deleteOldestResource();
            }
            wrapper = resourceWrapperMap.get(key);
        }

        wrapper.resource = this.processResource(input);
        wrapper.lastUpdated = System.currentTimeMillis();
        wrapper.state = ProcessingState.PROCESSED;

        synchronized(resourceWrapperMap) {
            log("Added resource for " + key + " (Size: " + resourceWrapperMap.size() + ")");
        }
    }


    /**
     * Returns the resource and refreshes its last updated time
     * @return The resource.
     * @throws NullPointerException If the resource does not exist
     */
    public Resource updateAndGetResource(Key key) {
        ProcessedPayloadWrapper<Resource> wrapper;
        synchronized(resourceWrapperMap) { wrapper = resourceWrapperMap.get(key); }
        if(wrapper == null) throw new NullPointerException();
        if(wrapper.state != ProcessingState.PROCESSED) throw new NullPointerException();

        Resource resource = wrapper.resource;
        wrapper.lastUpdated = System.currentTimeMillis();
        return resource;
    }

    private void deleteResourceByKey(Key key) {
        ProcessedPayloadWrapper<Resource> wrapper;
        synchronized(resourceWrapperMap) { wrapper = resourceWrapperMap.get(key); }
        if(wrapper == null) return;
        if(wrapper.state != ProcessingState.PROCESSED) return;

        Resource resource = wrapper.resource;
        this.deleteResource(resource);
        synchronized(resourceWrapperMap) {
            resourceWrapperMap.remove(key);
            log("Deleted resource " + key + " (Size: " + resourceWrapperMap.size() + ")");
        }
    }

    private void deleteOldestResource() {
        Key oldestKey = null;
        long oldest = Long.MAX_VALUE;

        synchronized(resourceWrapperMap) {
            for (Map.Entry<Key, ProcessedPayloadWrapper<Resource>> entry : resourceWrapperMap.entrySet()) {
                ProcessedPayloadWrapper<Resource> wrapper = entry.getValue();
                if (wrapper.state != ProcessingState.PROCESSED) continue;
                if (wrapper.lastUpdated < oldest) {
                    oldestKey = entry.getKey();
                    oldest = wrapper.lastUpdated;
                }
            }
        }

        if(oldestKey != null) {
            this.deleteResourceByKey(oldestKey);
        }
    }

    public void cleanUp() {
        long now = System.currentTimeMillis();
        ArrayList<Key> deleteList = new ArrayList<>();

        synchronized(resourceWrapperMap) {
            for (Map.Entry<Key, ProcessedPayloadWrapper<Resource>> entry : resourceWrapperMap.entrySet()) {
                ProcessedPayloadWrapper<Resource> wrapper = entry.getValue();
                if (wrapper.state != ProcessingState.PROCESSED) continue;
                if (wrapper.lastUpdated + this.expireMilliseconds > now) continue;
                deleteList.add(entry.getKey());
            }
        }

        if(!deleteList.isEmpty()) {
            log("Cleaning up...");
            for (Key key : deleteList) {
                this.deleteResourceByKey(key);
            }
        }
        this.updateProcessor();
    }

//    @Override
//    protected boolean processElement(CacheProcessRequest<Key, Input> element) {
//        try {
//            processResource(element.key, element.input);
//            return true;
//        } catch (Exception e) {
//            LOGGER.error("Error while processing a cache", e);
//            // Put the input back to the queue if something went wrong
//            return false;
//        }
//    }

//    @Override
//    protected void onElementProcessingFailed(CacheProcessRequest<Key, Input> element) {
//        resourcePreparingError(element.key);
//    }

    protected abstract void updateProcessor();
    protected abstract void offerToProcessor(Key key, Input input);
    /**
     * This method can take time as long as it wants, so try not to synchronize this with other objects
     * */
    protected abstract Resource processResource(Input input) throws Exception;
    protected abstract void deleteResource(Resource resource);

    @RequiredArgsConstructor
    protected static class CacheProcessRequest<Key, Input> {
        protected final Key key;
        protected final Input input;
    }

    @AllArgsConstructor
    private static class ProcessedPayloadWrapper<Resource> {
        private ProcessingState state;
        private Resource resource;
        private long lastUpdated;
    }
}
