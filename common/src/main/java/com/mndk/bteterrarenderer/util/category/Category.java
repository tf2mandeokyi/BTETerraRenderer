package com.mndk.bteterrarenderer.util.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mndk.bteterrarenderer.util.function.ThrowableBiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Getter
@JsonIgnoreProperties
@RequiredArgsConstructor
public class Category<T> {
    // internal map of id to wrapper
    private final Map<String, T> entries = new LinkedHashMap<>();

    public <E extends Throwable> void forEach(ThrowableBiConsumer<String, T, E> consumer) throws E {
        for (Entry<String, T> entry : entries.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    // accessors for entries
    public T get(String id) {
        return entries.get(id);
    }
    public void put(String id, T wrapper) {
        entries.put(id, wrapper);
    }
    public Set<Entry<String, T>> entrySet() {
        return entries.entrySet();
    }
}
