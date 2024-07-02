package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.datatype.DataIOManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

@Getter
public class DataBuffer {

    private static final UByteArray EMPTY_ORIGIN = UByteArray.create(0);

    private UByteArray data = EMPTY_ORIGIN;

    public DataBuffer() {}
    public DataBuffer(long size) {
        this.resize(size);
    }
    public DataBuffer(UByteArray data) {
        this.data = data;
    }
    public DataBuffer(InputStream inputStream) throws IOException {
        byte[] result = IOUtil.readAllBytes(inputStream);
        this.data = UByteArray.create(result);
    }
    public DataBuffer(ByteBuf buf) {
        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        this.data = UByteArray.create(result);
    }

    public Status update(DataBuffer buffer) {
        return update(buffer.data, buffer.data.size());
    }
    public Status update(UByteArray bytes, long size) {
        return update(bytes, 0, size);
    }
    public Status update(@Nullable UByteArray bytes, long offset, long size) {
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
        if(newSize == data.size()) return;
        UByteArray newOrigin = UByteArray.create(newSize);
        data.copyTo(0, newOrigin, 0, Math.min(newSize, data.size()));
        data = newOrigin;
    }

    public DataBuffer withOffset(ULong offset) {
        return this.withOffset(offset.longValue());
    }
    public DataBuffer withOffset(long offset) {
        return new DataBuffer(data.withOffset(offset));
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

    public <T> T read(DataIOManager<T> type, long index) {
        return type.read(data, index, Endian.LITTLE);
    }

    public <T> void write(DataIOManager<T> type, long index, T value) {
        type.write(data, index, value, Endian.LITTLE);
    }

    public <T, TArray> void write(DataType<T, TArray> type, long index, TArray value, long size) {
        for(int i = 0; i < size; ++i) {
            type.write(data, index + i, type.get(value, i), Endian.LITTLE);
        }
    }

    public UInt getLE16(long offset) { return data.getUInt16(offset, Endian.LITTLE).uIntValue(); }
    public UInt getLE24(long offset) { return data.getUInt24(offset, Endian.LITTLE); }
    public UInt getLE32(long offset) { return data.getUInt32(offset, Endian.LITTLE); }
    public void memPutLe16(long offset, UInt val) { data.setUInt16(offset, val.uShortValue(), Endian.LITTLE); }
    public void memPutLe24(long offset, UInt val) { data.setUInt24(offset, val, Endian.LITTLE); }
    public void memPutLe32(long offset, UInt val) { data.setUInt32(offset, val, Endian.LITTLE); }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
