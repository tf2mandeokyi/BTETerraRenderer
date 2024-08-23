package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import org.junit.Assert;
import org.junit.Test;

public class ShannonEntropyTest {

    @Test
    public void testBinaryEntropy() {
        Assert.assertEquals(0, ShannonEntropyTracker.computeBinary(UInt.of(0), UInt.of(0)), 0);
        Assert.assertEquals(0, ShannonEntropyTracker.computeBinary(UInt.of(10), UInt.of(0)), 0);
        Assert.assertEquals(0, ShannonEntropyTracker.computeBinary(UInt.of(10), UInt.of(10)), 0);
        Assert.assertEquals(1.0, ShannonEntropyTracker.computeBinary(UInt.of(10), UInt.of(5)), 1e-4);
    }

    @Test
    public void testStreamEntropy() {
        int[] symbols = new int[] { 1, 5, 1, 100, 2, 1 };
        Pointer<UInt> symbolsPointer = Pointer.wrapUnsigned(symbols);

        ShannonEntropyTracker entropyTracker = new ShannonEntropyTracker();
        Assert.assertEquals(0, entropyTracker.getNumberOfDataBits());

        int maxSymbol = 0;
        for(int i = 0; i < symbols.length; ++i) {
            if(symbols[i] > maxSymbol) {
                maxSymbol = symbols[i];
            }
            ShannonEntropyTracker.EntropyData entropyData = entropyTracker.push(symbolsPointer.add(i), 1);

            long streamEntropyBits = entropyTracker.getNumberOfDataBits();
            Assert.assertEquals(ShannonEntropyTracker.getNumberOfDataBits(entropyData), streamEntropyBits);

            long expectedEntropyBits = ShannonEntropyTracker.compute(symbolsPointer, i + 1, maxSymbol, null);
            Assert.assertEquals(expectedEntropyBits, streamEntropyBits, 2);
        }

        ShannonEntropyTracker entropyTracker2 = new ShannonEntropyTracker();
        entropyTracker2.push(symbolsPointer, symbols.length);
        long stream2EntropyBits = entropyTracker2.getNumberOfDataBits();
        Assert.assertEquals(entropyTracker.getNumberOfDataBits(), stream2EntropyBits);

        entropyTracker2.peek(symbolsPointer, 1);

        Assert.assertEquals(stream2EntropyBits, entropyTracker2.getNumberOfDataBits());
    }

}
