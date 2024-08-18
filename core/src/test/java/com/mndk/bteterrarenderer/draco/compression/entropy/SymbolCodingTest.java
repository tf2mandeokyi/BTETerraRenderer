package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.SymbolCodingMethod;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.printer.ByteTablePrinter;
import com.mndk.bteterrarenderer.printer.Printer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SymbolCodingTest {

    private static final int BITSTREAM_VERSION = DracoVersions.MESH_BIT_STREAM_VERSION;

    @SuppressWarnings("SameParameterValue")
    private <T, U> void testConvertToSymbolAndBack(DataNumberType<T> signedType, T value) {
        DataNumberType<U> symbolType = signedType.makeUnsigned();
        U symbol = BitUtils.convertSignedIntToSymbol(signedType, value, symbolType);
        T result = BitUtils.convertSymbolToSignedInt(symbolType, symbol, signedType);
        Assert.assertEquals(value, result);
    }

    @Test
    public void testLargeNumbers() {
        // This test verifies that SymbolCoding successfully encodes an array of large
        // numbers.
        int[] in = new int[] { 12345678, 1223333, 111, 5 };
        Pointer<UInt> inPointer = Pointer.wrapUnsigned(in);
        int numValues = in.length;
        EncoderBuffer eb = new EncoderBuffer();
        StatusAssert.assertOk(SymbolEncoding.encode(inPointer, numValues, 1, null, eb));

        Pointer<UInt> out = Pointer.newUIntArray(numValues);
        DecoderBuffer db = new DecoderBuffer();
        db.init(eb.getData(), eb.size());
        db.setBitstreamVersion(BITSTREAM_VERSION);
        StatusAssert.assertOk(SymbolDecoding.decode(UInt.of(numValues), 1, db, out));
        for(int i = 0; i < numValues; ++i) {
            Assert.assertEquals(UInt.of(in[i]), out.get(i));
        }
    }

    @Test
    public void testManyNumbers() {
        // This test verifies that SymbolCoding successfully encodes an array of
        // several numbers that repeat many times.
        // Value/frequency pairs.
        List<Pair<UInt, Integer>> in = new ArrayList<Pair<UInt, Integer>>() {{
            add(Pair.of(UInt.of(12), 1500));
            add(Pair.of(UInt.of(1025), 31000));
            add(Pair.of(UInt.of(7), 1));
            add(Pair.of(UInt.of(9), 5));
            add(Pair.of(UInt.of(0), 6432));
        }};
        CppVector<UInt> inValues = new CppVector<>(DataType.uint32());
        for (Pair<UInt, Integer> pair : in) {
            UInt left = pair.getLeft();
            int right = pair.getRight();
            for(int j = 0; j < right; ++j) {
                inValues.pushBack(left);
            }
        }
        for(int method = 0; method < SymbolCodingMethod.NUM_SYMBOL_CODING_METHODS; ++method) {
            // Test the encoding using all available symbol coding methods.
            Options options = new Options();
            SymbolCodingMethod symbolCodingMethod = SymbolCodingMethod.valueOf(UByte.of(method));
            Assert.assertNotNull(symbolCodingMethod);
            SymbolEncoding.setSymbolEncodingMethod(options, symbolCodingMethod);

            EncoderBuffer eb = new EncoderBuffer();
            Status status = SymbolEncoding.encode(inValues.getPointer(), (int) inValues.size(), 1, options, eb);
            StatusAssert.assertOk(status);

            Pointer<UInt> outValues = Pointer.wrapUnsigned(new int[(int) inValues.size()]);
            DecoderBuffer db = new DecoderBuffer();
            db.init(eb.getData(), eb.size());
            db.setBitstreamVersion(BITSTREAM_VERSION);
            StatusAssert.assertOk(SymbolDecoding.decode(UInt.of(inValues.size()), 1, db, outValues));

            for(int i = 0; i < inValues.size(); ++i) {
                Assert.assertEquals("Assertion fail on method=" + symbolCodingMethod + ", i=" + i,
                        inValues.get(i), outValues.get(i));
            }
        }
    }

    @Test
    public void testEmpty() {
        // This test verifies that SymbolCoding successfully encodes an empty array.
        EncoderBuffer eb = new EncoderBuffer();
        StatusAssert.assertOk(SymbolEncoding.encode(null, 0, 1, null, eb));
        DecoderBuffer db = new DecoderBuffer();
        db.init(eb.getData(), eb.size());
        db.setBitstreamVersion(BITSTREAM_VERSION);
        StatusAssert.assertOk(SymbolDecoding.decode(UInt.ZERO, 1, db, null));
    }

    @Test
    public void testOneSymbol() {
        // This test verifies that SymbolCoding successfully encodes a single
        // symbol.
        EncoderBuffer eb = new EncoderBuffer();
        int inLength = 1200;
        Pointer<UInt> inVector = Pointer.newUIntArray(inLength);
        StatusAssert.assertOk(SymbolEncoding.encode(inVector, inLength, 1, null, eb));
        ByteTablePrinter.print(Printer.stdout(), eb.getData(), eb.size());

        Pointer<UInt> out = Pointer.newUIntArray(inLength);
        DecoderBuffer db = new DecoderBuffer();
        db.init(eb.getData(), eb.size());
        db.setBitstreamVersion(BITSTREAM_VERSION);
        StatusAssert.assertOk(SymbolDecoding.decode(UInt.of(inLength), 1, db, out));
        for(int i = 0; i < inLength; ++i) {
            Assert.assertEquals(inVector.get(i), out.get(i));
        }
    }

    @Test
    public void testBitLengths() {
        // This test verifies that SymbolCoding successfully encodes symbols of
        // various bit lengths
        EncoderBuffer eb = new EncoderBuffer();
        CppVector<UInt> in = new CppVector<>(DataType.uint32());
        final int bitLengths = 18;
        for(int i = 0; i < bitLengths; ++i) {
            in.pushBack(UInt.of(1 << i));
        }
        Pointer<UInt> out = Pointer.wrapUnsigned(new int[(int) in.size()]);
        for(int i = 0; i < bitLengths; ++i) {
            eb.clear();
            Status status = SymbolEncoding.encode(in.getPointer(), i + 1, 1, null, eb);
            StatusAssert.assertOk(status);
            DecoderBuffer db = new DecoderBuffer();

            db.init(eb.getData(), eb.size());
            db.setBitstreamVersion(BITSTREAM_VERSION);
            StatusAssert.assertOk(SymbolDecoding.decode(UInt.of(i + 1), 1, db, out));
            for (int j = 0; j < i + 1; ++j) {
                Assert.assertEquals(in.get(j), out.get(j));
            }
        }
    }

    @Test
    public void testLargeNumberCondition() {
        // This test verifies that SymbolCoding successfully encodes large symbols
        // that are on the boundary between raw scheme and tagged scheme (18 bits).
        EncoderBuffer eb = new EncoderBuffer();
        final int numSymbols = 1000000;
        CppVector<UInt> in = new CppVector<>(DataType.uint32(), numSymbols, UInt.of(1 << 18));
        Status status = SymbolEncoding.encode(in.getPointer(), (int) in.size(), 1, null, eb);
        StatusAssert.assertOk(status);

        Pointer<UInt> out = Pointer.wrapUnsigned(new int[(int) in.size()]);
        DecoderBuffer db = new DecoderBuffer();
        db.init(eb.getData(), eb.size());
        db.setBitstreamVersion(BITSTREAM_VERSION);
        StatusAssert.assertOk(SymbolDecoding.decode(UInt.of(in.size()), 1, db, out));
        for(int i = 0; i < in.size(); ++i) {
            Assert.assertEquals(in.get(i), out.get(i));
        }
    }

    @Test
    public void testConversionFullRange() {
        testConvertToSymbolAndBack(DataType.int8(), (byte) -128);
        testConvertToSymbolAndBack(DataType.int8(), (byte) -127);
        testConvertToSymbolAndBack(DataType.int8(), (byte) -1);
        testConvertToSymbolAndBack(DataType.int8(), (byte) 0);
        testConvertToSymbolAndBack(DataType.int8(), (byte) 1);
        testConvertToSymbolAndBack(DataType.int8(), (byte) 127);
    }

}
