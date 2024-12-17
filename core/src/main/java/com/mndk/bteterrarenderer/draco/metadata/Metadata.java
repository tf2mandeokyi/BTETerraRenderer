/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.metadata;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Data;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

        private final CppVector<UByte> buffer;

        public <T> EntryValue(DataType<T> dataType, T data) {
            long dataTypeSize = dataType.byteSize();
            this.buffer = new CppVector<>(DataType.uint8(), dataTypeSize);
            dataType.write(this.buffer.getRawPointer(), data);
        }

        public <T> EntryValue(Pointer<T> data, int numEntries) {
            DataType<T> dataType = data.getType();
            long dataTypeSize = dataType.byteSize();
            this.buffer = new CppVector<>(DataType.uint8(), dataTypeSize * numEntries);
            PointerHelper.copyMultiple(data, this.buffer.getRawPointer(), numEntries);
        }

        public EntryValue(RawPointer data, long byteLength) {
            this.buffer = new CppVector<>(DataType.uint8(), byteLength);
            PointerHelper.rawCopy(data, this.buffer.getRawPointer(), byteLength);
        }

        public EntryValue(byte[] data) {
            this.buffer = new CppVector<>(DataType.uint8(), data.length);
            PointerHelper.rawCopy(data, 0, this.buffer.getRawPointer(), data.length);
        }

        public EntryValue(String value) {
            this(value.getBytes(StandardCharsets.UTF_8));
        }

        public <T> Status getValue(DataType<T> type, Consumer<T> outVal) {
            if (this.buffer.size() != type.byteSize()) {
                return Status.invalidParameter("Data size does not match the expected size");
            }
            outVal.accept(type.read(this.buffer.getRawPointer()));
            return Status.ok();
        }

        public <T> Status getValue(Pointer<T> outVal) {
            DataType<T> type = outVal.getType();
            if (this.buffer.size() % type.byteSize() != 0) {
                return Status.invalidParameter("Data size is not a multiple of the expected size");
            }
            long numEntries = this.buffer.size() / type.byteSize();
            PointerHelper.copyMultiple(this.buffer.getRawPointer(), outVal, (int) numEntries);
            return Status.ok();
        }

        public Status getValue(StringBuilder outVal) {
            if (this.buffer.isEmpty()) {
                return Status.invalidParameter("Data size is zero");
            }
            outVal.setLength(0);
            outVal.append(PointerHelper.rawToString(this.buffer.getRawPointer(), this.buffer.size()));
            return Status.ok();
        }

        public Status getValue(RawPointer outBuf) {
            PointerHelper.rawCopy(this.buffer.getRawPointer(), outBuf, this.buffer.size());
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
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(DataType.int32(), value);
    }

    public void addEntryIntArray(String name, int[] value) {
        this.entries.put(name, new EntryValue(Pointer.wrap(value), value.length));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryIntArray(String name, int[] value) {
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(Pointer.wrap(value));
    }

    public void addEntryDouble(String name, double value) {
        this.entries.put(name, new EntryValue(DataType.float64(), value));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDouble(String name, Consumer<Double> value) {
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(DataType.float64(), value);
    }

    public void addEntryDoubleArray(String name, double[] value) {
        this.entries.put(name, new EntryValue(Pointer.wrap(value), value.length));
    }

    /** Returns {@code false} if Metadata does not contain an entry with a key of {@code name}. */
    public Status getEntryDoubleArray(String name, double[] value) {
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(Pointer.wrap(value));
    }

    public void addEntryString(String name, String value) {
        this.entries.put(name, new EntryValue(value));
    }

    public Status getEntryString(String name, StringBuilder outVal) {
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(outVal);
    }

    public void addEntryBinary(String name, RawPointer buf, long size) {
        this.entries.put(name, new EntryValue(buf, size));
    }

    public Status getEntryBinary(String name, RawPointer outBuf) {
        EntryValue entry = this.entries.get(name);
        if (entry == null) return Status.dracoError("Metadata does not contain an entry with name " + name);
        return entry.getValue(outBuf);
    }

    public Status addSubMetadata(String name, Metadata metadata) {
        if (subMetadatas.containsKey(name)) {
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
