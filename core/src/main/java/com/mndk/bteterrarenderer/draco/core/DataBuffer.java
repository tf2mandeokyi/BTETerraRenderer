package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.vector.BigUByteVector;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Getter
public class DataBuffer {

    private final BigUByteVector data;

    public DataBuffer() {
        this.data = new BigUByteVector();
    }
    public DataBuffer(long size) {
        this.data = new BigUByteVector();
        this.resize(size);
    }
    public DataBuffer(BigUByteArray array) { this.data = new BigUByteVector(array); }
    public DataBuffer(InputStream inputStream) throws IOException { this(BigUByteArray.create(inputStream)); }
    public DataBuffer(ByteBuf buf) { this(BigUByteArray.create(buf)); }
    public DataBuffer(ByteBuffer buffer) { this(BigUByteArray.create(buffer)); }

    public Status update(DataBuffer buffer) {
        return this.update(buffer.data, buffer.data.size());
    }
    public Status update(BigUByteArray bytes, long size) {
        return this.update(bytes, 0, size);
    }
    public Status update(@Nullable BigUByteArray bytes, long offset, long size) {
        if(bytes == null) {
            if(size + offset < 0) {
                return Status.invalidParameter("Invalid offset: " + offset);
            }
            // If no data is provided, simply resize the buffer
            this.resize(size + offset);
        }
        else {
            if(size < 0) {
                return Status.invalidParameter("Invalid size: " + size);
            }
            if(size + offset > this.data.size()) {
                this.resize(size + offset);
            }
            bytes.copyTo(0, this.data, offset, size);
        }
        return Status.ok();
    }

    public void resize(long newSize) {
        data.resize(newSize);
    }

    public void copyFrom(long dstOffset, DataBuffer src, long srcOffset, long size) {
        src.data.copyTo(srcOffset, this.data, dstOffset, size);
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
        PointerHelper.copySingle(data.getRawPointer(index), out);
    }

    /** Reads data from the buffer. */
    public <T> void read(long index, Pointer<T> out, long count) {
        PointerHelper.copyMultiple(data.getRawPointer(index), out, count);
    }

    /** Writes data to the buffer. */
    public <T> void write(long index, DataType<T> type, T in) {
        PointerHelper.copySingle(type.newOwned(in), data.getRawPointer(index));
    }

    /** Writes data to the buffer. */
    public <T> void write(long index, Pointer<T> in, long count) {
        PointerHelper.copyMultiple(in, data.getRawPointer(index), count);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
