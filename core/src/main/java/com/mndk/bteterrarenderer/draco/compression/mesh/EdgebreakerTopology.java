package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.AbstractBorrowedRawByteArray;
import com.mndk.bteterrarenderer.datatype.pointer.AbstractOwnedRawByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EdgebreakerTopology {
    C((byte) 0x0, (byte) 1, (byte) 0), //     0
    S((byte) 0x1, (byte) 3, (byte) 1), // 0 0 1
    L((byte) 0x3, (byte) 3, (byte) 2), // 0 1 1
    R((byte) 0x5, (byte) 3, (byte) 3), // 1 0 1
    E((byte) 0x7, (byte) 3, (byte) 4), // 1 1 1
    /** A special value used to indicate an invalid symbol. */
    INVALID((byte) -1, (byte) 0, (byte) -1);

    public static final DataType<EdgebreakerTopology> BIT_PATTERN_TYPE = new BitPatternType();

    private final byte bitPattern;
    private final int bitPatternLength;
    private final int symbol;

    private static final EdgebreakerTopology[] BIT_PATTERN_TO_TOPOLOGY = { C, S, INVALID, L, INVALID, R, INVALID, E };
    public static EdgebreakerTopology fromBitPattern(int bitPattern) { return BIT_PATTERN_TO_TOPOLOGY[bitPattern]; }
    public static EdgebreakerTopology fromBitPattern(UInt bitPattern) { return fromBitPattern(bitPattern.intValue()); }

    private static final EdgebreakerTopology[] SYMBOL_TO_TOPOLOGY = { C, S, L, R, E };
    public static EdgebreakerTopology fromSymbol(int symbol) { return SYMBOL_TO_TOPOLOGY[symbol]; }

    private static class OwnedBitPattern extends AbstractOwnedRawByte<EdgebreakerTopology> {
        private OwnedBitPattern(EdgebreakerTopology value) { super(value.getBitPattern()); }
        @Override public DataType<EdgebreakerTopology> getType() { return BIT_PATTERN_TYPE; }
        @Override protected byte toRaw(EdgebreakerTopology value) { return value.getBitPattern(); }
        @Override protected EdgebreakerTopology fromRaw(byte raw) { return fromBitPattern(raw); }
    }

    private static class BorrowedBitPatternArray extends AbstractBorrowedRawByteArray<EdgebreakerTopology> {
        private BorrowedBitPatternArray(byte[] array, int offset) { super(array, offset); }
        private BorrowedBitPatternArray(int length, int offset) { super(new byte[length], offset); }
        @Override public DataType<EdgebreakerTopology> getType() { return BIT_PATTERN_TYPE; }
        @Override protected byte toRaw(EdgebreakerTopology value) { return value.getBitPattern(); }
        @Override protected EdgebreakerTopology fromRaw(byte raw) { return fromBitPattern(raw); }
        @Override public Pointer<EdgebreakerTopology> add(int offset) {
            return new BorrowedBitPatternArray(array, this.offset + offset);
        }
    }

    private static class BitPatternType implements DataType<EdgebreakerTopology> {
        @Override public EdgebreakerTopology defaultValue() { return INVALID; }
        @Override public boolean equals(EdgebreakerTopology left, EdgebreakerTopology right) { return left == right; }
        @Override public int hashCode(EdgebreakerTopology value) { return value.hashCode(); }
        @Override public String toString(EdgebreakerTopology value) { return value.name(); }
        @Override public long byteSize() { return 1; }
        @Override public EdgebreakerTopology parse(String value) { return fromBitPattern(Integer.parseInt(value)); }
        @Override public EdgebreakerTopology read(RawPointer src) { return fromBitPattern(src.getRawInt()); }
        @Override public void write(RawPointer dst, EdgebreakerTopology value) { dst.setRawByte(value.bitPattern); }
        @Override public Pointer<EdgebreakerTopology> newOwned(EdgebreakerTopology value) {
            return new OwnedBitPattern(value);
        }
        @Override public Pointer<EdgebreakerTopology> newArray(int length) {
            return new BorrowedBitPatternArray(length, 0);
        }
        @Override public Pointer<EdgebreakerTopology> castPointer(RawPointer pointer) {
            throw new UnsupportedOperationException();
        }
    }
}
