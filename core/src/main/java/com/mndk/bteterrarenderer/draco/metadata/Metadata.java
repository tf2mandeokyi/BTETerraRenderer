package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.draco.util.DracoCompressionException;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for holding generic metadata. It has a list of entries which consist of
 * an entry name and an entry value. Each Metadata could also have nested
 * metadata.
 */
@Data
public class Metadata {

    private final Map<String, Object> entries = new HashMap<>();
    private final Map<String, Metadata> subMetadatas = new HashMap<>();

    public Metadata() {}

    public Metadata(Metadata metadata) {
        this.entries.putAll(metadata.entries);
        this.subMetadatas.putAll(metadata.subMetadatas);
    }

    // Make this function private to avoid adding undefined data types.
    private <T> void addEntry(String name, T value) {
        entries.put(name, value);
    }

    // Make this function private to avoid adding undefined data types.
    @Nullable
    private <T> T getEntry(String name) {
        return BTRUtil.uncheckedCast(entries.get(name));
    }

    // In theory, we(google) support all types of data as long as it could be serialized
    // to binary data. We provide the following functions for inserting and
    // accessing entries of common data types. For now, developers need to know
    // the type of entries they are requesting.

    public void addEntryInt(String name, int value) {
        this.addEntry(name, value);
    }

    /**
     * @return {@code null} if Metadata does not contain an entry with a key of {@code name}.
     */
    @Nullable
    public Integer getEntryInt(String name) {
        return this.getEntry(name);
    }

    public void addEntryIntArray(String name, List<Integer> value) {
        this.addEntry(name, value);
    }

    /**
     * @return {@code null} if Metadata does not contain an entry with a key of {@code name}.
     */
    @Nullable
    public List<Integer> getEntryIntArray(String name) {
        return this.getEntry(name);
    }

    public void addEntryDouble(String name, double value) {
        this.addEntry(name, value);
    }

    /**
     * @return {@code null} if Metadata does not contain an entry with a key of {@code name}.
     */
    @Nullable
    public Double getEntryDouble(String name) {
        return this.getEntry(name);
    }

    public void addEntryDoubleArray(String name, List<Double> value) {
        this.addEntry(name, value);
    }

    /**
     * @return {@code null} if Metadata does not contain an entry with a key of {@code name}.
     */
    @Nullable
    public List<Double> getEntryDoubleArray(String name) {
        return this.getEntry(name);
    }

    public void addEntryString(String name, String value) {
        this.addEntry(name, value);
    }

    public String getEntryString(String name) {
        return this.getEntry(name);
    }

    public void addEntryBinary(String name, ByteBuf buf) {
        this.addEntry(name, buf);
    }

    /**
     * @return {@code null} if Metadata does not contain an entry with a key of {@code name}.
     */
    @Nullable
    public ByteBuf getEntryBinary(String name) {
        return this.getEntry(name);
    }

    public void addSubMetadata(String name, Metadata metadata) throws DracoCompressionException {
        if(subMetadatas.containsKey(name)) {
            throw new DracoCompressionException("Wrote over a sub-metadata with the same name");
        }
        this.subMetadatas.put(name, metadata);
    }

    public Metadata getSubMetadata(String name) {
        return this.subMetadatas.get(name);
    }

    public void removeEntry(String name) {
        this.entries.remove(name);
    }

    public int numEntries() {
        return this.entries.size();
    }
}
