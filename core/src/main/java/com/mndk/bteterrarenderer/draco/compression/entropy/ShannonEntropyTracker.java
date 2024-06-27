package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

import java.util.concurrent.atomic.AtomicReference;

public class ShannonEntropyTracker {

    public static class EntropyData {
        public double entropyNorm = 0;
        public int numValues = 0;
        public int maxSymbol = 0;
        public int numUniqueSymbols = 0;
    }

    private final CppVector<Integer> frequencies = CppVector.create(DataType.int32());
    private EntropyData entropyData = new EntropyData();

    public EntropyData push(CppVector<UInt> symbols, int numSymbols) {
        return updateSymbols(symbols, numSymbols, true);
    }

    public EntropyData peek(CppVector<UInt> symbols, int numSymbols) {
        return updateSymbols(symbols, numSymbols, false);
    }

    public EntropyData updateSymbols(CppVector<UInt> symbols, int numSymbols, boolean pushChanges) {
        EntropyData retData = entropyData;
        retData.numValues += numSymbols;
        for(int i = 0; i < numSymbols; ++i) {
            UInt symbol = symbols.get(i);
            if(frequencies.size() <= symbol.intValue()) {
                frequencies.resize(symbol.intValue() + 1, 0);
            }

            double oldSymbolEntropyNorm = 0;
            int frequency = frequencies.get(symbol);
            if(frequency > 1) {
                oldSymbolEntropyNorm = frequency * Ans.log2(frequency);
            } else if(frequency == 0) {
                retData.numUniqueSymbols++;
                if(symbol.gt(retData.maxSymbol)) {
                    retData.maxSymbol = symbol.intValue();
                }
            }
            frequency++;
            double newSymbolEntropyNorm = frequency * Ans.log2(frequency);
            retData.entropyNorm += newSymbolEntropyNorm - oldSymbolEntropyNorm;
            frequencies.set(symbol, frequency);
        }
        if(pushChanges) {
            entropyData = retData;
        } else {
            for(int i = 0; i < numSymbols; ++i) {
                UInt symbol = symbols.get(i);
                frequencies.set(symbol, frequencies.get(symbol) - 1);
            }
        }
        return retData;
    }

    public long getNumberOfDataBits() {
        return getNumberOfDataBits(this.entropyData);
    }

    public long getNumberOfRAnsTableBits() {
        return getNumberOfRAnsTableBits(this.entropyData);
    }

    public static long getNumberOfDataBits(EntropyData entropyData) {
        if(entropyData.numValues < 2) {
            return 0;
        }
        return (long) Math.ceil(
            entropyData.numValues * Ans.log2(entropyData.numValues) -
            entropyData.entropyNorm
        );
    }

    public static long getNumberOfRAnsTableBits(EntropyData entropyData) {
        return Ans.approximateRAnsFrequencyTableBits(entropyData.maxSymbol + 1, entropyData.numUniqueSymbols);
    }

    public static long computeEntropy(CppVector<UInt> symbols, int numSymbols, int maxValue,
                                      AtomicReference<Integer> outNumUniqueSymbols) {
        int numUniqueSymbols = 0;
        CppVector<Integer> symbolFrequencies = CppVector.create(DataType.int32(), maxValue + 1, 0);
        for(int i = 0; i < numSymbols; ++i) {
            UInt symbol = symbols.get(i);
            int oldValue = symbolFrequencies.get(symbol);
            symbolFrequencies.set(symbol, oldValue + 1);
        }
        double totalBits = 0;
        for(int i = 0; i < maxValue + 1; i++) {
            int symbolFrequency = symbolFrequencies.get(i);
            if(symbolFrequency > 0) {
                ++numUniqueSymbols;
                totalBits += symbolFrequency * Ans.log2((double) symbolFrequency / numSymbols);
            }
        }
        if(outNumUniqueSymbols != null) {
            outNumUniqueSymbols.set(numUniqueSymbols);
        }
        return (long) -totalBits;
    }

    public static double computeBinaryShannonEntropy(int numValues, int numTrueValues) {
        if(numValues == 0) return 0;

        // We can exit early if the data set has 0 entropy.
        if(numTrueValues == 0 || numValues == numTrueValues) return 0;
        double trueFreq = (double) numTrueValues / numValues;
        double falseFreq = 1.0 - trueFreq;
        return -(trueFreq * Ans.log2(trueFreq) + falseFreq * Ans.log2(falseFreq));
    }
}
