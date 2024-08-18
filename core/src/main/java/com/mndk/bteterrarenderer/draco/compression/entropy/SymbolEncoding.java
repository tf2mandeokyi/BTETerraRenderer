package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.SymbolCodingMethod;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class SymbolEncoding {

    public static final int MAX_TAG_SYMBOL_BIT_LENGTH = 32;
    public static final int MAX_RAW_ENCODING_BIT_LENGTH = 18;
    public static final int DEFAULT_SYMBOL_CODING_COMPRESSION_LEVEL = 7;

    public Status encode(Pointer<UInt> symbols, int numValues, int numComponents,
                         Options options, EncoderBuffer targetBuffer) {
        if(numValues < 0) return Status.invalidParameter("Invalid number of values");
        if(numValues == 0) return Status.ok();
        if(numComponents <= 0) numComponents = 1;
        CppVector<UInt> bitLengths = new CppVector<>(DataType.uint32());
        Pointer<UInt> maxValueRef = Pointer.newUInt();
        computeBitLengths(symbols, numValues, numComponents, bitLengths, maxValueRef);
        UInt maxValue = maxValueRef.get();

        // Approximate number of bits needed for storing the symbols using the tagged scheme.
        long taggedSchemeTotalBits = approximateTaggedSchemeBits(bitLengths, numComponents);

        // Approximate number of bits needed for storing the symbols using the raw scheme.
        Pointer<Integer> numUniqueSymbolsRef = Pointer.newInt();
        long rawSchemeTotalBits = approximateRawSchemeBits(symbols, numValues, maxValue, numUniqueSymbolsRef);
        int numUniqueSymbols = numUniqueSymbolsRef.get();

        // The maximum bit length of a single entry value that we can encode using the raw scheme.
        int maxValueBitLength = BitUtils.mostSignificantBit(DataType.uint32(), UInt.max(UInt.of(1), maxValue)) + 1;

        SymbolCodingMethod method;
        if(options != null && options.isOptionSet("symbol_encoding_method")) {
            int methodValue = options.getInt("symbol_encoding_method");
            method = SymbolCodingMethod.valueOf(methodValue);
            if(method == null) {
                return Status.invalidParameter("Invalid symbol encoding method: " + methodValue);
            }
        } else {
            if(taggedSchemeTotalBits < rawSchemeTotalBits || maxValueBitLength > MAX_RAW_ENCODING_BIT_LENGTH) {
                method = SymbolCodingMethod.SYMBOL_CODING_TAGGED;
            } else {
                method = SymbolCodingMethod.SYMBOL_CODING_RAW;
            }
        }

        // Use the tagged scheme.
        targetBuffer.encode(method.getValue());
        if(method == SymbolCodingMethod.SYMBOL_CODING_TAGGED) {
            return encodeTagged(RAnsSymbolEncoder::new, symbols, numValues, numComponents, bitLengths, targetBuffer);
        }
        if(method == SymbolCodingMethod.SYMBOL_CODING_RAW) {
            return encodeRaw(RAnsSymbolEncoder::new, symbols, numValues, maxValue, numUniqueSymbols, options, targetBuffer);
        }
        // Unknown method selected.
        return Status.invalidParameter("Unknown symbol encoding method: " + method);
    }

    private Status encodeTagged(Function<Integer, SymbolEncoder> encoderMaker,
                                Pointer<UInt> symbols, int numValues, int numComponents,
                                CppVector<UInt> bitLengths, EncoderBuffer targetBuffer) {
        StatusChain chain = new StatusChain();

        // Create entries for entropy coding. Each entry corresponds to a different
        // number of bits that are necessary to encode a given value. Every value
        // has at most 32 bits. Therefore, we need 32 different entries (for
        // bit_length [1-32]). For each entry we compute the frequency of a given
        // bit-length in our data set.
        // Set frequency for each entry to zero. (Java already does this)
        CppVector<ULong> frequencies = new CppVector<>(DataType.uint64(), MAX_TAG_SYMBOL_BIT_LENGTH, ULong.ZERO);

        // Compute the frequencies from input data.
        // Maximum integer value for the values across all components.
        for (int i = 0; i < bitLengths.size(); ++i) {
            // Update the frequency of the associated entry id.
            frequencies.set(bitLengths.get(i), val -> val.add(1));
        }

        // Create one extra buffer to store raw value.
        EncoderBuffer valueBuffer = new EncoderBuffer();
        // Number of expected bits we need to store the values (can be optimized if
        // needed).
        ULong valueBits = ULong.of((long) MAX_TAG_SYMBOL_BIT_LENGTH * numValues);

        // Create encoder for encoding the bit tags.
        SymbolEncoder tagEncoder = encoderMaker.apply(5);
        if(tagEncoder.create(frequencies.getPointer(), MAX_TAG_SYMBOL_BIT_LENGTH, targetBuffer).isError(chain)) return chain.get();

        // Start encoding bit tags.
        tagEncoder.startEncoding(targetBuffer);

        // Also start encoding the values.
        if(valueBuffer.startBitEncoding(valueBits.longValue(), false).isError(chain)) return chain.get();

        if(tagEncoder.needsReverseEncoding()) {
            // Encoder needs the values to be encoded in the reverse order.
            for (int i = numValues - numComponents; i >= 0; i -= numComponents) {
                int bitLength = bitLengths.get(i / numComponents).intValue();
                tagEncoder.encodeSymbol(UInt.of(bitLength));

                // Values are always encoded in the normal order
                int j = numValues - numComponents - i;
                int valueBitLength = bitLengths.get(j / numComponents).intValue();
                for (int c = 0; c < numComponents; ++c) {
                    Status status = valueBuffer.encodeLeastSignificantBits32(valueBitLength, symbols.get(j + c));
                    if(status.isError(chain)) return chain.get();
                }
            }
        } else {
            for (int i = 0; i < numValues; i += numComponents) {
                int bitLength = bitLengths.get(i / numComponents).intValue();
                // First encode the tag.
                tagEncoder.encodeSymbol(UInt.of(bitLength));
                // Now encode all values using the stored bit_length.
                for (int j = 0; j < numComponents; ++j) {
                    if(valueBuffer.encodeLeastSignificantBits32(bitLength, symbols.get(i + j)).isError(chain))
                        return chain.get();
                }
            }
        }
        tagEncoder.endEncoding(targetBuffer);
        valueBuffer.endBitEncoding();

        // Append the values to the end of the target buffer.
        return targetBuffer.encode(valueBuffer.getData(), valueBuffer.size());
    }

    private Status encodeRawInternal(Supplier<SymbolEncoder> encoderMaker,
                                     Pointer<UInt> symbols, int numValues, UInt maxEntryValue,
                                     EncoderBuffer targetBuffer) {
        StatusChain chain = new StatusChain();

        // Count the frequency of each entry value.
        CppVector<ULong> frequencies = new CppVector<>(DataType.uint64(), maxEntryValue.intValue() + 1, ULong.ZERO);
        for (int i = 0; i < numValues; ++i) {
            ULong freq = frequencies.get(symbols.get(i));
            frequencies.set(symbols.get(i), freq.add(1));
        }

        SymbolEncoder encoder = encoderMaker.get();
        if(encoder.create(frequencies.getPointer(), (int) frequencies.size(), targetBuffer).isError(chain)) return chain.get();
        encoder.startEncoding(targetBuffer);
        // Encode all values.
        if(encoder.needsReverseEncoding()) {
            for (int i = numValues - 1; i >= 0; --i) {
                encoder.encodeSymbol(symbols.get(i));
            }
        } else {
            for (int i = 0; i < numValues; ++i) {
                encoder.encodeSymbol(symbols.get(i));
            }
        }
        encoder.endEncoding(targetBuffer);
        return Status.ok();
    }

    private Status encodeRaw(Function<Integer, SymbolEncoder> encoderMaker,
                             Pointer<UInt> symbols, int numValues, UInt maxEntryValue, int numUniqueSymbols,
                             Options options, EncoderBuffer targetBuffer) {
        int symbolBits = 0;
        if(numUniqueSymbols > 0) {
            symbolBits = BitUtils.mostSignificantBit(DataType.uint32(), UInt.of(numUniqueSymbols));
        }
        int uniqueSymbolsBitLength = symbolBits + 1;
        // Currently, we don't support encoding of more than 2^18 unique symbols.
        if(uniqueSymbolsBitLength > MAX_RAW_ENCODING_BIT_LENGTH) {
            return Status.invalidParameter("Too long unique symbols bit length: " + uniqueSymbolsBitLength);
        }
        if(uniqueSymbolsBitLength <= 0) uniqueSymbolsBitLength = 1;
        int compressionLevel = DEFAULT_SYMBOL_CODING_COMPRESSION_LEVEL;
        if(options != null && options.isOptionSet("symbol_encoding_compression_level")) {
            compressionLevel = options.getInt("symbol_encoding_compression_level");
        }

        // Adjust the bit_length based on compression level.
        if(compressionLevel < 4) {
            uniqueSymbolsBitLength -= 2;
        } else if(compressionLevel < 6) {
            uniqueSymbolsBitLength -= 1;
        } else if(compressionLevel > 9) {
            uniqueSymbolsBitLength += 2;
        } else if(compressionLevel > 7) {
            uniqueSymbolsBitLength += 1;
        }
        // Clamp the bit_length to a valid range.
        uniqueSymbolsBitLength = Math.min(Math.max(1, uniqueSymbolsBitLength), MAX_RAW_ENCODING_BIT_LENGTH);
        targetBuffer.encode(UByte.of(uniqueSymbolsBitLength));

        // Use appropriate symbol encoder based on the maximum symbol bit length.
        int uniqueSymbolsBitLengthFinal = uniqueSymbolsBitLength;
        return encodeRawInternal(() -> encoderMaker.apply(uniqueSymbolsBitLengthFinal),
                symbols, numValues, maxEntryValue, targetBuffer);
    }

    private void computeBitLengths(Pointer<UInt> symbols, int numValues, int numComponents,
                                   CppVector<UInt> outBitLengths, Pointer<UInt> outMaxValue) {
        outBitLengths.reserve(numValues);
        outMaxValue.set(UInt.ZERO);
        // Maximum integer value across all components.
        for (int i = 0; i < numValues; i += numComponents) {
            // Get the maximum value for a given entry across all attribute components.
            UInt maxComponentValue = symbols.get(i);
            for (int j = 1; j < numComponents; ++j) {
                if (maxComponentValue.lt(symbols.get(i + j))) {
                    maxComponentValue = symbols.get(i + j);
                }
            }
            int valueMsbPos = 0;
            if (maxComponentValue.gt(0)) {
                valueMsbPos = BitUtils.mostSignificantBit(DataType.uint32(), maxComponentValue);
            }
            if (maxComponentValue.gt(outMaxValue.get())) {
                outMaxValue.set(maxComponentValue);
            }
            outBitLengths.pushBack(UInt.of(valueMsbPos + 1));
        }
    }

    private long approximateTaggedSchemeBits(CppVector<UInt> bitLengths, int numComponents) {
        // Compute the total bit length used by all values (the length of data encode
        // after tags).
        ULong totalBitLength = ULong.ZERO;
        for (int i = 0; i < bitLengths.size(); ++i) {
            totalBitLength = totalBitLength.add(DataType.uint32(), bitLengths.get(i));
        }
        // Compute the number of entropy bits for tags.
        Pointer<Integer> numUniqueSymbolsRef = Pointer.newInt();
        long tagBits = ShannonEntropyTracker.computeEntropy(
                bitLengths.getPointer(), (int) bitLengths.size(), 32, numUniqueSymbolsRef);
        int numUniqueSymbols = numUniqueSymbolsRef.get();
        long tagTableBits = Ans.approximateRAnsFrequencyTableBits(numUniqueSymbols, numUniqueSymbols);
        return tagBits + tagTableBits + totalBitLength.longValue() * numComponents;
    }

    private long approximateRawSchemeBits(Pointer<UInt> symbols, int numSymbols, UInt maxValue,
                                          Pointer<Integer> outNumUniqueSymbols) {
        Pointer<Integer> numUniqueSymbolsRef = Pointer.newInt();
        long dataBits = ShannonEntropyTracker.computeEntropy(symbols, numSymbols, maxValue.intValue(), numUniqueSymbolsRef);
        int numUniqueSymbols = numUniqueSymbolsRef.get();
        long tableBits = Ans.approximateRAnsFrequencyTableBits(maxValue.intValue(), numUniqueSymbols);
        outNumUniqueSymbols.set(numUniqueSymbols);
        return tableBits + dataBits;
    }

    public void setSymbolEncodingMethod(Options options, SymbolCodingMethod method) {
        options.setInt("symbol_encoding_method", method.getValue().intValue());
    }

    public boolean setSymbolEncodingCompressionLevel(Options options, int compressionLevel) {
        if (compressionLevel < 0 || compressionLevel > 10) {
            return false;
        }
        options.setInt("symbol_encoding_compression_level", compressionLevel);
        return true;
    }

}
