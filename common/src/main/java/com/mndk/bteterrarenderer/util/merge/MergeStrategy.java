package com.mndk.bteterrarenderer.util.merge;

/**
 * Strategy for merging new data into existing results.
 */
public interface MergeStrategy<T> {
    void merge(T original, T addition);
}
