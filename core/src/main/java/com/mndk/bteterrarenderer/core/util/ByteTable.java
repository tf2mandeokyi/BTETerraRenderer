package com.mndk.bteterrarenderer.core.util;

import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

@UtilityClass
public class ByteTable {

    public void print(ByteBuf buf, String prefix) {
        buf.markReaderIndex().markWriterIndex();
        byte[] bytes = IOUtil.readAllBytes(Unpooled.copiedBuffer(buf));
        buf.resetReaderIndex().resetWriterIndex();
        print(bytes, prefix);
    }

    public void print(ByteBuf buf) {
        print(buf, "");
    }

    public void print(ByteBuffer buffer, String prefix) {
        byte[] arr = IOUtil.readAllBytes(buffer);
        print(arr, prefix);
    }

    public void print(byte[] data, String prefix) {
        print(System.out, i -> data[Math.toIntExact(i)], 0, data.length, prefix);
    }

    public void print(UByteArray data, long offset, long size, String prefix) {
        print(System.out, index -> data.get(index).byteValue(), offset, size, prefix);
    }

    public void print(PrintStream out, Function<Long, Byte> data, long offset, long size, String prefix) {
        byte[] row = new byte[16];
        int rowCount = 0;
        out.print(prefix);
        out.println("          |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F | 0123456789ABCDEF ");
        out.print(prefix);
        out.println("----------+-------------------------------------------------+------------------");
        for(long i = 0; i < ((size + 15) / 16) * 16; i++) {
            if (i % 16 == 0) {
                out.print(prefix);
                out.printf(" %08x | ", i);
            }
            if(i < size) {
                out.printf("%02x ", data.apply(offset + i));
                row[(int) (i % 16)] = data.apply(offset + i);
                rowCount++;
            }
            else {
                out.print("   ");
            }
            if(i % 16 == 15) {
                out.print("| ");
                for(int j = 0; j < rowCount; j++) {
                    if(row[j] < 33 || 126 < row[j]) out.print(".");
                    else out.print((char) row[j]);
                }
                for(int j = rowCount; j < 16; j++) {
                    out.print(" ");
                }
                out.println();
                rowCount = 0;
            }
        }
    }
}
