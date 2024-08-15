package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitEncoder;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.entropy.ShannonEntropyTracker;
import com.mndk.bteterrarenderer.draco.core.BitUtils;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

import java.util.ArrayList;
import java.util.List;

public class MPSchemeConstrainedMultiParallelogramEncoder<DataT, CorrT> extends MPSchemeEncoder<DataT, CorrT> {

    private static class Error implements Comparable<Error> {
        int numBits;
        int residualError;
        @Override public int compareTo(Error o) {
            if(numBits < o.numBits) return -1;
            if(numBits > o.numBits) return 1;
            return Integer.compare(residualError, o.residualError);
        }
    }

    private final List<CppVector<Boolean>> isCreaseEdge = new ArrayList<>(); {
        for(int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            isCreaseEdge.add(new CppVector<>(DataType.bool()));
        }
    }
    private UByte selectedMode;
    private final ShannonEntropyTracker entropyTracker = new ShannonEntropyTracker();
    private final CppVector<UInt> entropySymbols = new CppVector<>(DataType.uint32());

    public MPSchemeConstrainedMultiParallelogramEncoder(PointAttribute attribute,
                                                        PSchemeEncodingTransform<DataT, CorrT> transform,
                                                        MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(inData, size, numComponents);
        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();

        // Predicted values for all simple parallelograms encountered at any given vertex.
        List<CppVector<DataT>> predVals = new ArrayList<>();
        for(int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            predVals.add(new CppVector<>(this.getDataType(), numComponents));
        }
        // Used to store predicted value for various multi-parallelogram predictions.
        CppVector<DataT> multiPredVals = new CppVector<>(this.getDataType(), numComponents);
        entropySymbols.resize(numComponents);

        // Struct for holding data about prediction configuration for different sets
        // of used parallelograms.
        class PredictionConfiguration {
            Error error = new Error();
            UByte configuration = UByte.ZERO;
            int numUsedParallelograms = 0;
            final CppVector<DataT> predictedValue = new CppVector<>(getDataType(), numComponents);
            final CppVector<Integer> residuals = new CppVector<>(DataType.int32(), numComponents);
        }

        // Bit-field used for computing permutations of excluded edges (parallelograms).
        Pointer<Boolean> excludedParallelograms = Pointer.wrap(
                new boolean[MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS]);

        // Data about the number of used parallelogram and total number of available
        // parallelogram for each context.
        long[] totalUsedParallelograms = new long[MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS];
        long[] totalParallelograms = new long[MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS];
        CppVector<Integer> currentResiduals = new CppVector<>(DataType.int32(), numComponents);

        // We start processing the vertices from the end because this prediction uses
        // data from previous entries that could be overwritten when an entry is processed.
        for(int p = (int) (this.getMeshData().getDataToCornerMap().size() - 1); p > 0; p--) {
            CornerIndex startCornerId = this.getMeshData().getDataToCornerMap().get(p);

            // Go over all corners attached to the vertex and compute the predicted
            // value from the parallelograms defined by their opposite faces.
            CornerIndex cornerId = startCornerId;
            int numParallelograms = 0;
            boolean firstPass = true;
            while(cornerId.isValid()) {
                Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                        p, cornerId, table, vertexToDataMap, inData, numComponents,
                        predVals.get(numParallelograms).getPointer());
                if(status.isOk()) {
                    ++numParallelograms;
                    if (numParallelograms == MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS) {
                        break;
                    }
                }

                // Proceed to the next corner attached to the vertex.
                cornerId = firstPass ? table.swingLeft(cornerId) : table.swingRight(cornerId);
                if(cornerId.equals(startCornerId)) {
                    break;
                }
                if(cornerId.isInvalid() && firstPass) {
                    firstPass = false;
                    cornerId = table.swingRight(startCornerId);
                }
            }
            // Offset to the target (destination) vertex.
            int dstOffset = p * numComponents;

            // Compute delta coding error.
            int srcOffset = (p - 1) * numComponents;
            Error error = computeError(inData.add(srcOffset), inData.add(dstOffset),
                    currentResiduals.getPointer(), numComponents);

            if(numParallelograms > 0) {
                totalParallelograms[numParallelograms - 1] += numParallelograms;
                long newOverheadBits = computeOverheadBits(
                        totalUsedParallelograms[numParallelograms - 1],
                        totalParallelograms[numParallelograms - 1]);
                error.numBits += (int) newOverheadBits;
            }

            // Variable for holding the best configuration that has been found so far.
            PredictionConfiguration bestPrediction = new PredictionConfiguration();
            bestPrediction.error = error;
            bestPrediction.configuration = UByte.ZERO;
            bestPrediction.numUsedParallelograms = 0;
            bestPrediction.predictedValue.assign(inData.add(srcOffset), numComponents);
            bestPrediction.residuals.assign(currentResiduals.getPointer(), currentResiduals.size());

            // Compute prediction error for different cases of used parallelograms.
            for(int numUsedParallelograms = 1; numUsedParallelograms <= numParallelograms; ++numUsedParallelograms) {
                for(int j = 0; j < numParallelograms; ++j) {
                    excludedParallelograms.set(j, true);
                }
                for(int j = 0; j < numUsedParallelograms; ++j) {
                    excludedParallelograms.set(j, false);
                }
                do {
                    for(int j = 0; j < numComponents; ++j) {
                        multiPredVals.set(j, this.getDataType().from(0));
                    }
                    UByte configuration = UByte.ZERO;
                    for(int j = 0; j < numParallelograms; ++j) {
                        if(excludedParallelograms.get(j)) {
                            continue;
                        }
                        for(int c = 0; c < numComponents; ++c) {
                            multiPredVals.set(c, this.getDataType().add(multiPredVals.get(c), predVals.get(j).get(c)));
                        }
                        configuration = configuration.or(UByte.of(1 << j));
                    }

                    for(int j = 0; j < numComponents; j++) {
                        multiPredVals.set(j, this.getDataType().div(multiPredVals.get(j), numUsedParallelograms));
                    }
                    error = computeError(multiPredVals.getPointer(), inData.add(dstOffset),
                            currentResiduals.getPointer(), numComponents);
                    long newOverheadBits = computeOverheadBits(
                            totalUsedParallelograms[numParallelograms - 1] + numUsedParallelograms,
                            totalParallelograms[numParallelograms - 1]);

                    // Add overhead bits to the total error.
                    error.numBits += (int) newOverheadBits;
                    if(error.compareTo(bestPrediction.error) < 0) {
                        bestPrediction.error = error;
                        bestPrediction.configuration = configuration;
                        bestPrediction.numUsedParallelograms = numUsedParallelograms;
                        bestPrediction.predictedValue.assign(multiPredVals.getPointer(), multiPredVals.size());
                        bestPrediction.residuals.assign(currentResiduals.getPointer(), currentResiduals.size());
                    }
                } while(PointerHelper.nextPermutation(excludedParallelograms, numParallelograms, Boolean::compare));
            }
            if(numParallelograms > 0) {
                totalUsedParallelograms[numParallelograms - 1] += bestPrediction.numUsedParallelograms;
            }

            // Update the entropy stream by adding selected residuals as symbols to the stream.
            for(int i = 0; i < numComponents; ++i) {
                UInt symbol = BitUtils.convertSignedIntToSymbol(DataType.int32(), bestPrediction.residuals.get(i),
                        DataType.uint32());
                entropySymbols.set(i, symbol);
            }
            entropyTracker.push(entropySymbols.getPointer(), numComponents);

            for(int i = 0; i < numParallelograms; i++) {
                if((bestPrediction.configuration.and(1 << i)).equals(0)) {
                    isCreaseEdge.get(numParallelograms - 1).pushBack(true);
                } else {
                    isCreaseEdge.get(numParallelograms - 1).pushBack(false);
                }
            }
            this.getTransform().computeCorrection(inData.add(dstOffset), bestPrediction.predictedValue.getPointer(),
                    outCorr.add(dstOffset));
        }
        // First element is always fixed because it cannot be predicted.
        for(int i = 0; i < numComponents; ++i) {
            predVals.get(0).set(i, this.getDataType().from(0));
        }
        this.getTransform().computeCorrection(inData, predVals.get(0).getPointer(), outCorr);
        return Status.ok();
    }

    @Override
    public Status encodePredictionData(EncoderBuffer buffer) {
        for(int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            int numUsedParallelograms = i + 1;
            buffer.encodeVarint(DataType.uint32(), UInt.of(isCreaseEdge.get(i).size()));
            if(!isCreaseEdge.get(i).isEmpty()) {
                RAnsBitEncoder encoder = new RAnsBitEncoder();
                encoder.startEncoding();

                for(int j = (int) (isCreaseEdge.get(i).size() - numUsedParallelograms); j >= 0; j -= numUsedParallelograms) {
                    for(int k = 0; k < numUsedParallelograms; ++k) {
                        encoder.encodeBit(isCreaseEdge.get(i).get(j + k));
                    }
                }
                encoder.endEncoding(buffer);
            }
        }
        return super.encodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_CONSTRAINED_MULTI_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }

    private long computeOverheadBits(long totalUsedParallelograms, long totalParallelogram) {
        double entropy = ShannonEntropyTracker.computeBinaryShannonEntropy(
                UInt.of(totalParallelogram), UInt.of(totalUsedParallelograms));
        return (long) Math.ceil(totalParallelogram * entropy);
    }

    private Error computeError(Pointer<DataT> predictedVal, Pointer<DataT> actualVal, Pointer<Integer> outResiduals,
                               int numComponents) {
        Error error = new Error();
        DataNumberType<DataT> dataType = this.getDataType();

        for(int i = 0; i < numComponents; ++i) {
            int dif = dataType.toInt(dataType.sub(predictedVal.get(i), actualVal.get(i)));
            error.residualError += Math.abs(dif);
            outResiduals.set(i, dif);
            entropySymbols.set(i, BitUtils.convertSignedIntToSymbol(DataType.int32(), dif, DataType.uint32()));
        }

        ShannonEntropyTracker.EntropyData entropyData = entropyTracker.peek(entropySymbols.getPointer(), numComponents);
        error.numBits = (int) (ShannonEntropyTracker.getNumberOfDataBits(entropyData) +
                ShannonEntropyTracker.getNumberOfRAnsTableBits(entropyData));
        return error;
    }
}
