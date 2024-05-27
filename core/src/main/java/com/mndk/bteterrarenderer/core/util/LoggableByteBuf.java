package com.mndk.bteterrarenderer.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

@Deprecated
public class LoggableByteBuf extends ByteBuf {
    private final ByteBuf delegate;

    public LoggableByteBuf(ByteBuf delegate) {
        this.delegate = delegate;
    }
    
    public int capacity() { return delegate.capacity(); }
    public ByteBuf capacity(int newCapacity) { return delegate.capacity(newCapacity); }
    public int maxCapacity() { return delegate.maxCapacity(); }
    public ByteBufAllocator alloc() { return delegate.alloc(); }
    public ByteOrder order() { return delegate.order(); }
    public ByteBuf order(ByteOrder endianness) { return delegate.order(endianness); }
    public ByteBuf unwrap() { return delegate.unwrap(); }
    public boolean isDirect() { return delegate.isDirect(); }
    public boolean isReadOnly() { return delegate.isReadOnly(); }
    public ByteBuf asReadOnly() { return delegate.asReadOnly(); }
    public int readerIndex() { return delegate.readerIndex(); }
    public ByteBuf readerIndex(int readerIndex) { return delegate.readerIndex(readerIndex); }
    public int writerIndex() { return delegate.writerIndex(); }
    public ByteBuf writerIndex(int writerIndex) { return delegate.writerIndex(writerIndex); }
    public ByteBuf setIndex(int readerIndex, int writerIndex) { return delegate.setIndex(readerIndex, writerIndex); }
    public int readableBytes() { return delegate.readableBytes(); }
    public int writableBytes() { return delegate.writableBytes(); }
    public int maxWritableBytes() { return delegate.maxWritableBytes(); }
    public boolean isReadable() { return delegate.isReadable(); }
    public boolean isReadable(int size) { return delegate.isReadable(size); }
    public boolean isWritable() { return delegate.isWritable(); }
    public boolean isWritable(int size) { return delegate.isWritable(size); }
    public ByteBuf clear() { return delegate.clear(); }
    public ByteBuf markReaderIndex() { return delegate.markReaderIndex(); }
    public ByteBuf resetReaderIndex() { return delegate.resetReaderIndex(); }
    public ByteBuf markWriterIndex() { return delegate.markWriterIndex(); }
    public ByteBuf resetWriterIndex() { return delegate.resetWriterIndex(); }
    public ByteBuf discardReadBytes() { return delegate.discardReadBytes(); }
    public ByteBuf discardSomeReadBytes() { return delegate.discardSomeReadBytes(); }
    public ByteBuf ensureWritable(int minWritableBytes) { return delegate.ensureWritable(minWritableBytes); }
    public int ensureWritable(int minWritableBytes, boolean force) { return delegate.ensureWritable(minWritableBytes, force); }
    public boolean getBoolean(int index) { return delegate.getBoolean(index); }
    public byte getByte(int index) { return delegate.getByte(index); }
    public short getUnsignedByte(int index) { return delegate.getUnsignedByte(index); }
    public short getShort(int index) { return delegate.getShort(index); }
    public short getShortLE(int index) { return delegate.getShortLE(index); }
    public int getUnsignedShort(int index) { return delegate.getUnsignedShort(index); }
    public int getUnsignedShortLE(int index) { return delegate.getUnsignedShortLE(index); }
    public int getMedium(int index) { return delegate.getMedium(index); }
    public int getMediumLE(int index) { return delegate.getMediumLE(index); }
    public int getUnsignedMedium(int index) { return delegate.getUnsignedMedium(index); }
    public int getUnsignedMediumLE(int index) { return delegate.getUnsignedMediumLE(index); }
    public int getInt(int index) { return delegate.getInt(index); }
    public int getIntLE(int index) { return delegate.getIntLE(index); }
    public long getUnsignedInt(int index) { return delegate.getUnsignedInt(index); }
    public long getUnsignedIntLE(int index) { return delegate.getUnsignedIntLE(index); }
    public long getLong(int index) { return delegate.getLong(index); }
    public long getLongLE(int index) { return delegate.getLongLE(index); }
    public char getChar(int index) { return delegate.getChar(index); }
    public float getFloat(int index) { return delegate.getFloat(index); }
    public double getDouble(int index) { return delegate.getDouble(index); }
    public ByteBuf getBytes(int index, ByteBuf dst) { return delegate.getBytes(index, dst); }
    public ByteBuf getBytes(int index, ByteBuf dst, int length) { return delegate.getBytes(index, dst, length); }
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) { return delegate.getBytes(index, dst, dstIndex, length); }
    public ByteBuf getBytes(int index, byte[] dst) { return delegate.getBytes(index, dst); }
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) { return delegate.getBytes(index, dst, dstIndex, length); }
    public ByteBuf getBytes(int index, ByteBuffer dst) { return delegate.getBytes(index, dst); }
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException { return delegate.getBytes(index, out, length); }
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException { return delegate.getBytes(index, out, length);}
    public int getBytes(int index, FileChannel out, long position, int length) throws IOException { return delegate.getBytes(index, out, position, length); }
    public CharSequence getCharSequence(int index, int length, Charset charset) { return delegate.getCharSequence(index, length, charset); }
    public ByteBuf setBoolean(int index, boolean value) { return delegate.setBoolean(index, value); }
    public ByteBuf setByte(int index, int value) { return delegate.setByte(index, value); }
    public ByteBuf setShort(int index, int value) { return delegate.setShort(index, value); }
    public ByteBuf setShortLE(int index, int value) { return delegate.setShortLE(index, value); }
    public ByteBuf setMedium(int index, int value) { return delegate.setMedium(index, value); }
    public ByteBuf setMediumLE(int index, int value) { return delegate.setMediumLE(index, value); }
    public ByteBuf setInt(int index, int value) { return delegate.setInt(index, value); }
    public ByteBuf setIntLE(int index, int value) { return delegate.setIntLE(index, value); }
    public ByteBuf setLong(int index, long value) { return delegate.setLong(index, value); }
    public ByteBuf setLongLE(int index, long value) { return delegate.setLongLE(index, value); }
    public ByteBuf setChar(int index, int value) { return delegate.setChar(index, value); }
    public ByteBuf setFloat(int index, float value) { return delegate.setFloat(index, value); }
    public ByteBuf setDouble(int index, double value) { return delegate.setDouble(index, value); }
    public ByteBuf setBytes(int index, ByteBuf src) { return delegate.setBytes(index, src); }
    public ByteBuf setBytes(int index, ByteBuf src, int length) { return delegate.setBytes(index, src, length); }
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) { return delegate.setBytes(index, src, srcIndex, length); }
    public ByteBuf setBytes(int index, byte[] src) { return delegate.setBytes(index, src); }
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) { return delegate.setBytes(index, src, srcIndex, length); }
    public ByteBuf setBytes(int index, ByteBuffer src) { return delegate.setBytes(index, src); }
    public int setBytes(int index, InputStream in, int length) throws IOException { return delegate.setBytes(index, in, length); }
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException { return delegate.setBytes(index, in, length); }
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException { return delegate.setBytes(index, in, position, length); }
    public ByteBuf setZero(int index, int length) { return delegate.setZero(index, length); }
    public int setCharSequence(int index, CharSequence sequence, Charset charset) { return delegate.setCharSequence(index, sequence, charset); }
    public boolean readBoolean() { return this.logMessage("readBoolean", delegate.readBoolean()); }
    public byte readByte() { return this.logMessage("readByte", delegate.readByte()); }
    public short readUnsignedByte() { return this.logMessage("readUnsignedByte", delegate.readUnsignedByte()); }
    public short readShort() { return this.logMessage("readShort", delegate.readShort()); }
    public short readShortLE() { return this.logMessage("readShortLE", delegate.readShortLE()); }
    public int readUnsignedShort() { return this.logMessage("readUnsignedShort", delegate.readUnsignedShort()); }
    public int readUnsignedShortLE() { return this.logMessage("readUnsignedShortLE", delegate.readUnsignedShortLE()); }
    public int readMedium() { return this.logMessage("readMedium", delegate.readMedium()); }
    public int readMediumLE() { return this.logMessage("readMediumLE", delegate.readMediumLE()); }
    public int readUnsignedMedium() { return this.logMessage("readUnsignedMedium", delegate.readUnsignedMedium()); }
    public int readUnsignedMediumLE() { return this.logMessage("readUnsignedMediumLE", delegate.readUnsignedMediumLE()); }
    public int readInt() { return this.logMessage("readInt", delegate.readInt()); }
    public int readIntLE() { return this.logMessage("readIntLE", delegate.readIntLE()); }
    public long readUnsignedInt() { return this.logMessage("readUnsignedInt", delegate.readUnsignedInt()); }
    public long readUnsignedIntLE() { return this.logMessage("readUnsignedIntLE", delegate.readUnsignedIntLE()); }
    public long readLong() { return this.logMessage("readLong", delegate.readLong()); }
    public long readLongLE() { return this.logMessage("readLongLE", delegate.readLongLE()); }
    public char readChar() { return this.logMessage("readChar", delegate.readChar()); }
    public float readFloat() { return this.logMessage("readFloat", delegate.readFloat()); }
    public double readDouble() { return this.logMessage("readDouble", delegate.readDouble()); }
    public ByteBuf readBytes(int length) {
        this.logMessage("readBytes", length);
        ByteBuf result = delegate.readBytes(length);
        ByteTable.print(result.copy(), "[IO]     ");
        return new LoggableByteBuf(result);
    }
    public ByteBuf readSlice(int length) { return delegate.readSlice(length); }
    public ByteBuf readRetainedSlice(int length) { return delegate.readRetainedSlice(length); }
    public ByteBuf readBytes(ByteBuf dst) { return delegate.readBytes(dst); }
    public ByteBuf readBytes(ByteBuf dst, int length) { return delegate.readBytes(dst, length); }
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) { return delegate.readBytes(dst, dstIndex, length); }
    public ByteBuf readBytes(byte[] dst) { return delegate.readBytes(dst); }
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) { return delegate.readBytes(dst, dstIndex, length); }
    public ByteBuf readBytes(ByteBuffer dst) { return delegate.readBytes(dst); }
    public ByteBuf readBytes(OutputStream out, int length) throws IOException { return delegate.readBytes(out, length); }
    public int readBytes(GatheringByteChannel out, int length) throws IOException { return delegate.readBytes(out, length); }
    public CharSequence readCharSequence(int length, Charset charset) { return delegate.readCharSequence(length, charset); }
    public int readBytes(FileChannel out, long position, int length) throws IOException { return delegate.readBytes(out, position, length); }
    public ByteBuf skipBytes(int length) { return delegate.skipBytes(length); }
    public ByteBuf writeBoolean(boolean value) { return delegate.writeBoolean(value); }
    public ByteBuf writeByte(int value) { return delegate.writeByte(value); }
    public ByteBuf writeShort(int value) { return delegate.writeShort(value); }
    public ByteBuf writeShortLE(int value) { return delegate.writeShortLE(value); }
    public ByteBuf writeMedium(int value) { return delegate.writeMedium(value); }
    public ByteBuf writeMediumLE(int value) { return delegate.writeMediumLE(value); }
    public ByteBuf writeInt(int value) { return delegate.writeInt(value); }
    public ByteBuf writeIntLE(int value) { return delegate.writeIntLE(value); }
    public ByteBuf writeLong(long value) { return delegate.writeLong(value); }
    public ByteBuf writeLongLE(long value) { return delegate.writeLongLE(value); }
    public ByteBuf writeChar(int value) { return delegate.writeChar(value); }
    public ByteBuf writeFloat(float value) { return delegate.writeFloat(value); }
    public ByteBuf writeDouble(double value) { return delegate.writeDouble(value); }
    public ByteBuf writeBytes(ByteBuf src) { return delegate.writeBytes(src); }
    public ByteBuf writeBytes(ByteBuf src, int length) { return delegate.writeBytes(src, length); }
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) { return delegate.writeBytes(src, srcIndex, length); }
    public ByteBuf writeBytes(byte[] src) { return delegate.writeBytes(src); }
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) { return delegate.writeBytes(src, srcIndex, length); }
    public ByteBuf writeBytes(ByteBuffer src) { return delegate.writeBytes(src); }
    public int writeBytes(InputStream in, int length) throws IOException { return delegate.writeBytes(in, length); }
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException { return delegate.writeBytes(in, length); }
    public int writeBytes(FileChannel in, long position, int length) throws IOException { return delegate.writeBytes(in, position, length); }
    public ByteBuf writeZero(int length) { return delegate.writeZero(length); }
    public int writeCharSequence(CharSequence sequence, Charset charset) { return delegate.writeCharSequence(sequence, charset); }
    public int indexOf(int fromIndex, int toIndex, byte value) { return delegate.indexOf(fromIndex, toIndex, value); }
    public int bytesBefore(byte value) { return delegate.bytesBefore(value); }
    public int bytesBefore(int length, byte value) { return delegate.bytesBefore(length, value); }
    public int bytesBefore(int index, int length, byte value) { return delegate.bytesBefore(index, length, value); }
    public int forEachByte(ByteProcessor processor) { return delegate.forEachByte(processor); }
    public int forEachByte(int index, int length, ByteProcessor processor) { return delegate.forEachByte(index, length, processor); }
    public int forEachByteDesc(ByteProcessor processor) { return delegate.forEachByteDesc(processor); }
    public int forEachByteDesc(int index, int length, ByteProcessor processor) { return delegate.forEachByteDesc(index, length, processor); }
    public ByteBuf copy() { return delegate.copy(); }
    public ByteBuf copy(int index, int length) { return delegate.copy(index, length); }
    public ByteBuf slice() { return delegate.slice(); }
    public ByteBuf retainedSlice() { return delegate.retainedSlice(); }
    public ByteBuf slice(int index, int length) { return delegate.slice(index, length); }
    public ByteBuf retainedSlice(int index, int length) { return delegate.retainedSlice(index, length); }
    public ByteBuf duplicate() { return delegate.duplicate(); }
    public ByteBuf retainedDuplicate() { return delegate.retainedDuplicate(); }
    public int nioBufferCount() { return delegate.nioBufferCount(); }
    public ByteBuffer nioBuffer() { return delegate.nioBuffer(); }
    public ByteBuffer nioBuffer(int index, int length) { return delegate.nioBuffer(index, length); }
    public ByteBuffer internalNioBuffer(int index, int length) { return delegate.internalNioBuffer(index, length); }
    public ByteBuffer[] nioBuffers() { return delegate.nioBuffers(); }
    public ByteBuffer[] nioBuffers(int index, int length) { return delegate.nioBuffers(index, length); }
    public boolean hasArray() { return delegate.hasArray(); }
    public byte[] array() { return delegate.array(); }
    public int arrayOffset() { return delegate.arrayOffset(); }
    public boolean hasMemoryAddress() { return delegate.hasMemoryAddress(); }
    public long memoryAddress() { return delegate.memoryAddress(); }
    public String toString(Charset charset) { return delegate.toString(charset); }
    public String toString(int index, int length, Charset charset) { return delegate.toString(index, length, charset); }
    public int hashCode() { return delegate.hashCode(); }
    public boolean equals(Object obj) { return delegate.equals(obj); }
    public int compareTo(ByteBuf buffer) { return delegate.compareTo(buffer); }
    public String toString() { return delegate.toString(); }
    public ByteBuf retain(int increment) { return delegate.retain(increment); }
    public int refCnt() { return delegate.refCnt(); }
    public ByteBuf retain() { return delegate.retain(); }
    public ByteBuf touch() { return delegate.touch(); }
    public ByteBuf touch(Object hint) { return delegate.touch(hint); }
    public boolean release() { return delegate.release(); }
    public boolean release(int decrement) { return delegate.release(decrement); }

    private <T> T logMessage(String message, T object) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        System.out.println("[IO] " + message + " -> " + object);
        for(int i = 3; i <= 4; i++) {
            StackTraceElement element = trace[i];
            System.out.println("[IO]     " + element.getClassName() + "." + element.getMethodName() +
                    "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
        }
        return object;
    }
}