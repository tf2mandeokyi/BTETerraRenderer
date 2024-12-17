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

package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DataBuffer {

    private final CppVector<UByte> data;

    public DataBuffer() {
        this.data = new CppVector<>(DataType.uint8());
    }
    public DataBuffer(long size) {
        this.data = new CppVector<>(DataType.uint8(), size);
    }
    public DataBuffer(RawPointer pointer, long size) {
        this.data = new CppVector<>(DataType.uint8(), size);
        PointerHelper.rawCopy(pointer, this.data.getRawPointer(), size);
    }
    public DataBuffer(byte[] data) {
        this.data = new CppVector<>(DataType.uint8(), data.length);
        PointerHelper.rawCopy(data, 0, this.data.getRawPointer(), data.length);
    }
    public DataBuffer(InputStream inputStream) throws IOException {this(IOUtil.readAllBytes(inputStream)); }
    public DataBuffer(ByteBuf buf) {this(IOUtil.readAllBytes(buf)); }
    public DataBuffer(ByteBuffer buffer) {this(IOUtil.readAllBytes(buffer)); }

    public Pointer<UByte> getData() {
        return data.getPointer();
    }

    public Status update(DataBuffer buffer) {
        return this.update(buffer.data.getRawPointer(), buffer.data.size());
    }
    public Status update(RawPointer data, long size) {
        return this.update(data, 0, size);
    }
    public Status update(@Nullable RawPointer data, long offset, long size) {
        if (data == null) {
            if (size + offset < 0) {
                return Status.invalidParameter("Invalid offset: " + offset);
            }
            // If no data is provided, simply resize the buffer
            this.resize(size + offset);
        }
        else {
            if (size < 0) {
                return Status.invalidParameter("Invalid size: " + size);
            }
            if (size + offset > this.data.size()) {
                this.resize(size + offset);
            }
            PointerHelper.rawCopy(data, this.data.getRawPointer().rawAdd(offset), size);
        }
        return Status.ok();
    }

    public void resize(long newSize) {
        data.resize(newSize);
    }

    public void copyFrom(long dstOffset, DataBuffer src, long srcOffset, long size) {
        RawPointer srcPointer = src.data.getRawPointer().rawAdd(srcOffset);
        RawPointer dstPointer = this.data.getRawPointer().rawAdd(dstOffset);
        PointerHelper.rawCopy(srcPointer, dstPointer, size);
    }

    public long size() {
        return this.data.size();
    }

    public UByte get(long index) {
        return data.get(index);
    }

    public void set(long index, UByte value) {
        data.set(index, value);
    }

    /** Reads data from the buffer. */
    public <T> void read(long index, Pointer<T> out) {
        PointerHelper.copySingle(data.getRawPointer().rawAdd(index), out);
    }

    /** Reads data from the buffer. */
    public <T> void read(long index, Pointer<T> out, long count) {
        PointerHelper.copyMultiple(data.getRawPointer().rawAdd(index), out, count);
    }

    /** Writes data to the buffer. */
    public <T> void write(long index, DataType<T> type, T in) {
        PointerHelper.copySingle(type.newOwned(in), data.getRawPointer().rawAdd(index));
    }

    /** Writes data to the buffer. */
    public <T> void write(long index, Pointer<T> in, long count) {
        PointerHelper.copyMultiple(in, data.getRawPointer().rawAdd(index), count);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
