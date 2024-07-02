package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Data;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

        private final UByteArray buffer;

        public <T> EntryValue(DataType<T, ?> dataType, T data) {
            long dataTypeSize = dataType.size();
            this.buffer = UByteArray.create(dataTypeSize);
            dataType.write(this.buffer, 0, data, Endian.LITTLE);
        }

        public <T, TArray> EntryValue(DataType<T, TArray> dataType, TArray data, int numEntries) {
            long dataTypeSize = dataType.size();
            this.buffer = UByteArray.create(dataTypeSize * numEntries);
            for(int i = 0; i < numEntries; ++i) {
                dataType.write(this.buffer, i * dataTypeSize, dataType.get(data, i), Endian.LITTLE);
            }
        }

        public EntryValue(UByteArray data) {
            this.buffer = data;
        }

        public EntryValue(String value) {
            this(UByteArray.create(value, StandardCharsets.UTF_8));
        }

        public <T> Status getValue(DataType<T, ?> type, Consumer<T> outVal) {
            if(this.buffer.size() != type.size()) {
                return Status.invalidParameter("Data size does not match the expected size");
            }
            outVal.accept(type.read(this.buffer, 0, Endian.LITTLE));
            return Status.ok();
        }

        public <T, TArray> Status getValue(DataType<T, TArray> type, TArray outVal) {
            if(this.buffer.size() % type.size() != 0) {
                return Status.invalidParameter("Data size is not a multiple of the expected size");
            }
            long numEntries = this.buffer.size() / type.size();
            for(int i = 0; i < numEntries; ++i) {
                type.set(outVal, i, type.read(this.buffer, i * type.size(), Endian.LITTLE));
            }
            return Status.ok();
        }

        public Status getValue(StringBuilder outVal) {
            if(this.buffer.size() == 0) {
                return Status.invalidParameter("Data size is zero");
            }
            outVal.setLength(0);
            outVal.append(this.buffer.decode());
            return Status.ok();
        }

        public Status getValue(AtomicReference<UByteArray> outBuf) {
            UByteArray array = UByteArray.create(this.buffer.size());
            array.copyTo(0, this.buffer, 0, this.buffer.size());
            outBuf.set(array);
            return Status.ok();
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
        this.entries.put(name, new EntryValue(DataType.int32(), value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryInt(String name, Consumer<Integer> value) {
        return this.entries.get(name).getValue(DataType.int32(), value);
    }

    public void addEntryIntArray(String name, int[] value) {
        this.entries.put(name, new EntryValue(DataType.int32(), value, value.length));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryIntArray(String name, int[] value) {
        return this.entries.get(name).getValue(DataType.int32(), value);
    }

    public void addEntryDouble(String name, double value) {
        this.entries.put(name, new EntryValue(DataType.float64(), value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDouble(String name, Consumer<Double> value) {
        return this.entries.get(name).getValue(DataType.float64(), value);
    }

    public void addEntryDoubleArray(String name, double[] value) {
        this.entries.put(name, new EntryValue(DataType.float64(), value, value.length));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDoubleArray(String name, double[] value) {
        return this.entries.get(name).getValue(DataType.float64(), value);
    }

    public void addEntryString(String name, String value) {
        this.entries.put(name, new EntryValue(value));
    }

    public Status getEntryString(String name, StringBuilder outVal) {
        return this.entries.get(name).getValue(outVal);
    }

    public void addEntryBinary(String name, UByteArray buf) {
        this.entries.put(name, new EntryValue(buf));
    }

    public Status getEntryBinary(String name, AtomicReference<UByteArray> outBuf) {
        return this.entries.get(name).getValue(outBuf);
    }

    public Status addSubMetadata(String name, Metadata metadata) {
        if(subMetadatas.containsKey(name)) {
            return Status.invalidParameter("Metadata already contains sub-metadata with name " + name);
        }
        this.subMetadatas.put(name, metadata);
        return Status.ok();
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
