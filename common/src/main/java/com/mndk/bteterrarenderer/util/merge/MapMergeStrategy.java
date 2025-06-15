package com.mndk.bteterrarenderer.util.merge;

import java.util.Map;

/**
 * MergeStrategy that puts all entries from addition Map into original Map.
 */
public class MapMergeStrategy<K, V> implements MergeStrategy<Map<K, V>> {
    @Override
    public void merge(Map<K, V> original, Map<K, V> addition) {
        original.putAll(addition);
    }
}
