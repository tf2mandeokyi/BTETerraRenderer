package com.mndk.bteterrarenderer.util.loader;

/**
 * Generic interface for configuration serializers handling load and save operations.
 */
public interface ConfigLoader<T> {
    void load(T content);
    void save(T content);
}
