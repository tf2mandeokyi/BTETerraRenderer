package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class RAnsSymbolEncoder implements SymbolEncoder {

    private final int ransPrecision;

    private final CppVector<RAnsSymbol> probabilityTable = new CppVector<>(RAnsSymbol::new);
    /** The number of symbols in the input alphabet. */
    private UInt numSymbols;
    /** Expected number of bits that is needed to encode the input. */
    private ULong numExpectedBits;

    private final RAnsEncoder ans;
    private ULong bufferOffset;

    public RAnsSymbolEncoder(int uniqueSymbolsBitLength) {
        int ransPrecisionBits = Ans.computeRAnsPrecisionFromUniqueSymbolsBitLength(uniqueSymbolsBitLength);
        this.ransPrecision = 1 << ransPrecisionBits;
        this.ans = new RAnsEncoder(ransPrecisionBits);
    }

    @Override
    public Status create(CppVector<ULong> frequencies, int numSymbols, EncoderBuffer buffer) {
        ULong totalFreq = ULong.ZERO;
        int maxValidSymbol = 0;
        for (int i = 0; i < numSymbols; ++i) {
            totalFreq = totalFreq.add(frequencies.get(i));
            if (frequencies.get(i).gt(0)) {
                maxValidSymbol = i;
            }
        }
        numSymbols = maxValidSymbol + 1;
        this.numSymbols = UInt.of(numSymbols);
        probabilityTable.resize(numSymbols);
        final double totalFreqD = totalFreq.doubleValue();
        final double ransPrecisionD = ransPrecision;
        // Compute probabilities by rescaling the normalized frequencies into interval
        // [1, ransPrecision - 1]. The total probability needs to be equal to
        // ransPrecision.
        int totalRansProb = 0;
        for(int i = 0; i < numSymbols; ++i) {
            ULong freq = frequencies.get(i);

            // Normalized probability.
            double prob = freq.doubleValue() / totalFreqD;

            // RAns probability in range of [1, ransPrecision - 1].
            UInt ransProb = UInt.of((int) (prob * ransPrecisionD + 0.5));
            if(ransProb.equals(0) && freq.gt(0)) {
                ransProb = UInt.of(1);
            }
            probabilityTable.get(i).prob = ransProb;
            totalRansProb += ransProb.intValue();
        }
        // Because of rounding errors, the total precision may not be exactly accurate
        // and we may need to adjust the entries a little bit.
        if(totalRansProb != ransPrecision) {
            CppVector<Integer> sortedProbabilities = new CppVector<>(DataType.int32(), numSymbols);
            for(int i = 0; i < numSymbols; ++i) {
                sortedProbabilities.set(i, i);
            }
            sortedProbabilities.sort((a, b) -> probabilityTable.get(a).prob.compareTo(probabilityTable.get(b).prob));
            if(totalRansProb < ransPrecision) {
                // This happens rather infrequently, just add the extra needed precision
                // to the most frequent symbol.
                RAnsSymbol symbol = probabilityTable.get(sortedProbabilities.back());
                symbol.prob = symbol.prob.add(ransPrecision - totalRansProb);
            } else {
                // We have over-allocated the precision, which is quite common.
                // Rescale the probabilities of all symbols.
                int error = totalRansProb - ransPrecision;
                while(error > 0) {
                    double actTotalProbD = totalRansProb;
                    double actRelErrorD = ransPrecisionD / actTotalProbD;
                    for(int j = numSymbols - 1; j > 0; --j) {
                        int symbolId = sortedProbabilities.get(j);
                        RAnsSymbol symbol = probabilityTable.get(symbolId);
                        if(symbol.prob.le(1)) {
                            if(j == numSymbols - 1) {
                                return Status.ioError("Most frequent symbol is empty");
                            }
                            break;
                        }
                        int newProb = (int) Math.floor(actRelErrorD * symbol.prob.intValue());
                        // int32_t fix = probability_table_[symbol_id].prob - new_prob;
                        int fix = symbol.prob.intValue() - newProb;
                        if(fix == 0) {
                            fix = 1;
                        }
                        if(fix >= symbol.prob.intValue()) {
                            fix = symbol.prob.sub(1).intValue();
                        }
                        if(fix > error) {
                            fix = error;
                        }
                        symbol.prob = symbol.prob.sub(fix);
                        totalRansProb -= fix;
                        error -= fix;
                        if(totalRansProb == ransPrecision) {
                            break;
                        }
                    }
                }
            }
        }

        // Compute the cumulative probability (cdf).
        UInt totalProb = UInt.ZERO;
        for(int i = 0; i < numSymbols; ++i) {
            RAnsSymbol symbol = probabilityTable.get(i);
            symbol.cumProb = totalProb;
            totalProb = totalProb.add(symbol.prob);
        }
        if(!totalProb.equals(ransPrecision)) {
            return Status.ioError("Total probability is not equal to ransPrecision");
        }

        // Estimate the number of bits needed to encode the input.
        double numBits = 0;
        for(int i = 0; i < numSymbols; ++i) {
            RAnsSymbol symbol = probabilityTable.get(i);
            if(symbol.prob.equals(0)) {
                continue;
            }
            double normProb = symbol.prob.doubleValue() / ransPrecisionD;
            numBits += frequencies.get(i).doubleValue() * Ans.log2(normProb);
        }
        numExpectedBits = ULong.of((long) Math.ceil(-numBits));
        return this.encodeTable(buffer);
    }

    @Override
    public boolean needsReverseEncoding() {
        return true;
    }

    @Override
    public void startEncoding(EncoderBuffer buffer) {
        // Allocate extra storage just in case.
        ULong requiredBits = numExpectedBits.mul(2).add(32);

        bufferOffset = ULong.of(buffer.size());
        long requiredBytes = requiredBits.add(7).div(8).longValue();
        buffer.resize(bufferOffset.add(requiredBytes).add(bufferOffset.getType().byteSize()).longValue());
        RawPointer data = buffer.getData();
        ans.writeInit(data.rawAdd(bufferOffset.longValue()));
    }

    @Override
    public void encodeSymbol(UInt symbol) {
        ans.ransWrite(probabilityTable.get(symbol));
    }

    @Override
    public void endEncoding(EncoderBuffer buffer) {
        RawPointer dataBuffer = buffer.getData();
        long src = bufferOffset.longValue();

        ULong bytesWritten = ans.writeEnd();
        EncoderBuffer varSizeBuffer = new EncoderBuffer();
        varSizeBuffer.encodeVarint(bytesWritten);
        UInt sizeLen = UInt.of(varSizeBuffer.size());
        long dst = src + sizeLen.longValue();
        dataBuffer.rawAdd(src).rawCopyTo(dataBuffer.rawAdd(dst), bytesWritten.longValue());

        // Store the size of the encoded data.
        varSizeBuffer.getData().rawCopyTo(dataBuffer.rawAdd(src), sizeLen.longValue());

        buffer.resize(bufferOffset.add(bytesWritten).add(sizeLen.uLongValue()).longValue());
    }

    private Status encodeTable(EncoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        if(buffer.encodeVarint(numSymbols).isError(chain)) return chain.get();
        // Use varint encoding for the probabilities (first two bits represent the
        // number of bytes used - 1).
        for(UInt i = UInt.ZERO; i.lt(numSymbols); i = i.add(1)) {
            UInt prob = probabilityTable.get(i).prob;
            int numExtraBytes = 0;
            if(prob.ge(1 << 6)) {
                numExtraBytes++;
                if(prob.ge(1 << 14)) {
                    numExtraBytes++;
                    if(prob.ge(1 << 22)) {
                        // The maximum number of precision bits is 20, so we should not really
                        // get to this point.
                        return Status.ioError("The maximum number of precision bits is 20");
                    }
                }
            }
            if(prob.equals(0)) {
                // When the probability of the symbol is 0, set the first two bits to 1
                // (unique identifier) and use the remaining 6 bits to store the offset
                // to the next symbol with non-zero probability.
                UInt offset = UInt.ZERO;
                for(; offset.lt((1 << 6) - 1); offset = offset.add(1)) {
                    // Note: we don't have to check whether the next symbol id is larger
                    // than num_symbols_ because we know that the last symbol always has
                    // non-zero probability.
                    UInt nextProb = probabilityTable.get(i.add(offset).add(1)).prob;
                    if(nextProb.gt(0)) {
                        break;
                    }
                }
                UByte temp = offset.shl(2).or(3).uByteValue();
                if(buffer.encode(temp).isError(chain)) return chain.get();
                i = i.add(offset);
            } else {
                // Encode the first byte (including the number of extra bytes).
                UByte temp = prob.shl(2).or(numExtraBytes & 3).uByteValue();
                if(buffer.encode(temp).isError(chain)) return chain.get();
                // Encode the extra bytes
                for(int b = 0; b < numExtraBytes; ++b) {
                    UByte temp1 = prob.shr(8 * (b + 1) - 2).uByteValue();
                    if(buffer.encode(temp1).isError(chain)) return chain.get();
                }
            }
        }
        return Status.ok();
    }

}
