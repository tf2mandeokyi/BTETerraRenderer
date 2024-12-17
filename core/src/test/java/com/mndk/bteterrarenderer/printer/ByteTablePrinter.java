package com.mndk.bteterrarenderer.printer;

import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.util.function.Function;

public class ByteTablePrinter {

    public static void print(Printer out, RawPointer pointer, long size) {
        print(out, pointer::getRawByte, size);
    }

    public static void print(Printer out, Function<Long, Byte> data, long size) {
        byte[] row = new byte[16];
        int rowCount = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("          |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F | 0123456789ABCDEF \n");
        sb.append("----------+-------------------------------------------------+------------------\n");
        for (long i = 0; i < ((size + 15) / 16) * 16; i++) {
            if (i % 16 == 0) {
                sb.append(String.format(" %08x | ", i));
            }
            if (i < size) {
                sb.append(String.format("%02x ", data.apply(i)));
                row[(int) (i % 16)] = data.apply(i);
                rowCount++;
            }
            else {
                sb.append("   ");
            }
            if (i % 16 == 15) {
                sb.append("| ");
                for (int j = 0; j < rowCount; j++) {
                    if (row[j] < 33 || 126 < row[j]) sb.append(".");
                    else sb.append((char) row[j]);
                }
                for (int j = rowCount; j < 16; j++) {
                    sb.append(" ");
                }
                sb.append("\n");
                rowCount = 0;
            }
        }
        out.print(sb.toString());
    }
}
