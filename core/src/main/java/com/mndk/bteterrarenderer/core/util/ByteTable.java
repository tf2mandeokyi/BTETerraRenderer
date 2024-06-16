package com.mndk.bteterrarenderer.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;

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
        print(data, 0, data.length, prefix);
    }

    public void print(byte[] data, int offset, int size, String prefix) {
        byte[] row = new byte[16];
        int rowCount = 0;
        System.out.print(prefix);
        System.out.println("          |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F | 0123456789ABCDEF ");
        System.out.print(prefix);
        System.out.println("----------+-------------------------------------------------+------------------");
        for(int i = 0; i < ((size + 15) / 16) * 16; i++) {
            if (i % 16 == 0) {
                System.out.print(prefix);
                System.out.printf(" %08x | ", i);
            }
            if(i < size) {
                System.out.printf("%02x ", data[offset + i]);
                row[i % 16] = data[offset + i];
                rowCount++;
            }
            else {
                System.out.print("   ");
            }
            if(i % 16 == 15) {
                System.out.print("| ");
                for(int j = 0; j < rowCount; j++) {
                    if(row[j] < 33 || 126 < row[j]) System.out.print(".");
                    else System.out.print((char) row[j]);
                }
                for(int j = rowCount; j < 16; j++) {
                    System.out.print(" ");
                }
                System.out.println();
                rowCount = 0;
            }
        }
    }
}
