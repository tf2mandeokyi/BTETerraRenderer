package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.DataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Data;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class for holding generic metadata. It has a list of entries which consist of
 * an entry name and an entry value. Each Metadata could also have nested
 * metadata.
 */
@Data
public class Metadata {

    /**
     * Class for storing a value of an entry in Metadata. Internally it is
     * represented by a buffer of data. It can be accessed by various data types,
     * e.g. int, float, binary data or string.
     */
    @Getter
    public static class EntryValue {

        private final DataBuffer buffer;

        public <T> EntryValue(DataType<T> dataType, T data) {
            int dataTypeSize = dataType.size();
            this.buffer = new DataBuffer(dataTypeSize);
            dataType.setBuf(this.buffer, 0, data);
        }

        public <T> EntryValue(DataType<T> dataType, Function<Integer, T> data, int numEntries) {
            int dataTypeSize = dataType.size();
            this.buffer = new DataBuffer(dataTypeSize * numEntries);
            for(int i = 0; i < numEntries; ++i) {
                dataType.setBuf(this.buffer, i * dataTypeSize, data.apply(i));
            }
        }
        public <T> EntryValue(DataType<T> dataType, List<T> data) {
            this(dataType, data::get, data.size());
        }

        public EntryValue(byte[] data) {
            this.buffer = new DataBuffer();
            this.buffer.update(data, data.length);
        }

        public EntryValue(String value) {
            this(value.getBytes(StandardCharsets.UTF_8));
        }

        public <T> Status getValue(DataType<T> type, Consumer<T> outVal) {
            if(this.buffer.size() != type.size()) {
                return new Status(Status.Code.INVALID_PARAMETER, "Data size does not match the expected size");
            }
            outVal.accept(type.getBuf(this.buffer, 0));
            return Status.OK;
        }

        public <T> Status getValue(DataType<T> type, List<T> outVal) {
            if(this.buffer.size() % type.size() != 0) {
                return new Status(Status.Code.INVALID_PARAMETER, "Data size is not a multiple of the expected size");
            }
            int numEntries = this.buffer.size() / type.size();
            outVal.clear();
            for(int i = 0; i < numEntries; ++i) {
                outVal.add(type.getBuf(this.buffer, i * type.size()));
            }
            return Status.OK;
        }

        public Status getValue(StringBuilder outVal) {
            if(this.buffer.size() == 0) {
                return new Status(Status.Code.INVALID_PARAMETER, "Data size is zero");
            }
            outVal.setLength(0);
            outVal.append(new String(this.buffer.getData(), StandardCharsets.UTF_8));
            return Status.OK;
        }

        public Status getValue(AtomicReference<byte[]> outBuf) {
            byte[] copy = new byte[this.buffer.size()];
            System.arraycopy(this.buffer.getData(), 0, copy, 0, this.buffer.size());
            outBuf.set(copy);
            return Status.OK;
        }

        @Override
        public int hashCode() {
            return this.buffer.hashCode();
        }
    }

    private final Map<String, EntryValue> entries = new HashMap<>();
    private final Map<String, Metadata> subMetadatas = new HashMap<>();

    public Metadata() {}

    public Metadata(Metadata metadata) {
        this.entries.putAll(metadata.entries);
        this.subMetadatas.putAll(metadata.subMetadatas);
    }

    public void addEntryInt(String name, int value) {
        this.entries.put(name, new EntryValue(DataType.INT32, value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryInt(String name, Consumer<Integer> value) {
        return this.entries.get(name).getValue(DataType.INT32, value);
    }

    public void addEntryIntArray(String name, List<Integer> value) {
        this.entries.put(name, new EntryValue(DataType.INT32, value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryIntArray(String name, List<Integer> value) {
        return this.entries.get(name).getValue(DataType.INT32, value);
    }

    public void addEntryDouble(String name, double value) {
        this.entries.put(name, new EntryValue(DataType.FLOAT64, value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDouble(String name, Consumer<Double> value) {
        return this.entries.get(name).getValue(DataType.FLOAT64, value);
    }

    public void addEntryDoubleArray(String name, List<Double> value) {
        this.entries.put(name, new EntryValue(DataType.FLOAT64, value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDoubleArray(String name, List<Double> value) {
        return this.entries.get(name).getValue(DataType.FLOAT64, value);
    }

    public void addEntryString(String name, String value) {
        this.entries.put(name, new EntryValue(value));
    }

    public Status getEntryString(String name, StringBuilder outVal) {
        return this.entries.get(name).getValue(outVal);
    }

    public void addEntryBinary(String name, byte[] buf) {
        this.entries.put(name, new EntryValue(buf));
    }

    public Status getEntryBinary(String name, AtomicReference<byte[]> outBuf) {
        return this.entries.get(name).getValue(outBuf);
    }

    public Status addSubMetadata(String name, Metadata metadata) {
        if(subMetadatas.containsKey(name)) {
            return new Status(Status.Code.INVALID_PARAMETER, "Metadata already contains sub-metadata with name " + name);
        }
        this.subMetadatas.put(name, metadata);
        return Status.OK;
    }

    public Metadata getSubMetadata(String name) {
        return this.subMetadatas.get(name);
    }

    public void removeEntry(String name) {
        this.entries.remove(name);
    }

    public int getNumEntries() {
        return this.entries.size();
    }
}
