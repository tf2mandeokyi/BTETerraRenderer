package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.core.util.ByteTable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class DataBuffer {

    private static final byte[] EMPTY_ORIGIN = new byte[0];

    private byte[] data = EMPTY_ORIGIN;

    public DataBuffer() {}
    public DataBuffer(int size) {
        this.resize(size);
    }

    public Status update(DataBuffer buffer) {
        return update(buffer.data, buffer.data.length);
    }
    public Status update(byte[] bytes, int size) {
        return update(bytes, 0, size);
    }
    public Status update(@Nullable byte[] bytes, int offset, int size) {
        if(bytes == null) {
            if(size + offset < 0) {
                return new Status(Status.Code.INVALID_PARAMETER, "Invalid offset: " + offset);
            }
            // If no data is provided, simply resize the buffer
            this.resize(size + offset);
        }
        else {
            if(size < 0) {
                return new Status(Status.Code.INVALID_PARAMETER, "Invalid size: " + size);
            }
            if(size + offset > this.data.length) {
                this.resize(size + offset);
            }
            System.arraycopy(bytes, 0, this.data, offset, size);
        }
        return Status.OK;
    }

    public void resize(int newSize) {
        if(newSize > data.length) {
            byte[] newOrigin = new byte[newSize];
            System.arraycopy(data, 0, newOrigin, 0, data.length);
            data = newOrigin;
        }
    }

    public void printAsTable(String prefix) {
        ByteTable.print(data, 0, data.length, prefix);
    }

    public ByteBuf wrappedBuf(int offset, int size) {
        return Unpooled.wrappedBuffer(data, offset, size);
    }

    public void copy(int dstOffset, DataBuffer src, int srcOffset, int size) {
        System.arraycopy(src.data, srcOffset, this.data, dstOffset, size);
    }

    public int size() {
        return this.data.length;
    }

    public byte get(int index) {
        return data[index];
    }

    public void set(int index, byte value) {
        data[index] = value;
    }

    @Override
    public int hashCode() {
        int result = size();
        for (byte element : data) result = 31 * result + element;
        return result;
    }
}
