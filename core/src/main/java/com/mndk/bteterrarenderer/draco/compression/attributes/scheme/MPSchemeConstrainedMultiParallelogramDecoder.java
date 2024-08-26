/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

import java.util.ArrayList;
import java.util.List;

public class MPSchemeConstrainedMultiParallelogramDecoder<DataT, CorrT> extends MPSchemeDecoder<DataT, CorrT> {

    private final List<CppVector<Boolean>> isCreaseEdge = new ArrayList<>(); {
        for (int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            isCreaseEdge.add(new CppVector<>(DataType.bool()));
        }
    }
    // private Mode selectedMode; // This is never used

    public MPSchemeConstrainedMultiParallelogramDecoder(PointAttribute attribute,
                                                        PSchemeDecodingTransform<DataT, CorrT> transform,
                                                        MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);

        // Predicted values for all simple parallelograms encountered at any given vertex.
        DataNumberType<DataT> dataType = this.getDataType();
        List<CppVector<DataT>> predVals = new ArrayList<>();
        for (int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            predVals.add(new CppVector<>(this.getDataType(), numComponents, dataType.from(0)));
        }
        this.getTransform().computeOriginalValue(predVals.get(0).getPointer(), inCorr, outData);

        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();

        // Current position in the isCreaseEdge array for each context.
        CppVector<Integer> isCreaseEdgePos = new CppVector<>(DataType.int32(), MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS, 0);

        // Used to store predicted value for multi-parallelogram prediction.
        CppVector<DataT> multiPredVals = new CppVector<>(dataType, numComponents);

        int cornerMapSize = (int) this.getMeshData().getDataToCornerMap().size();
        for (int p = 1; p < cornerMapSize; ++p) {
            CornerIndex startCornerId = this.getMeshData().getDataToCornerMap().get(p);
            CornerIndex cornerId = startCornerId;
            int numParallelograms = 0;
            boolean firstPass = true;
            while (cornerId.isValid()) {
                Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                        p, cornerId, table, vertexToDataMap, outData, numComponents,
                        predVals.get(numParallelograms).getPointer());
                if (status.isError()) {
                    // Parallelogram prediction applied and stored in predVals[numParallelograms]
                    ++numParallelograms;
                    // Stop processing when we reach the maximum number of allowed parallelograms.
                    if (numParallelograms == MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS) {
                        break;
                    }
                }

                // Proceed to the next corner attached to the vertex. First swing left
                // and if we reach a boundary, swing right from the start corner.
                if (firstPass) {
                    cornerId = table.swingLeft(cornerId);
                } else {
                    cornerId = table.swingRight(cornerId);
                }
                if (cornerId.equals(startCornerId)) {
                    break;
                }
                if (cornerId.isInvalid() && firstPass) {
                    firstPass = false;
                    cornerId = table.swingRight(startCornerId);
                }
            }

            // Check which of the available parallelograms are actually used and compute the final predicted value.
            int numUsedParallelograms = 0;
            if (numParallelograms > 0) {
                for (int i = 0; i < numComponents; ++i) {
                    multiPredVals.set(i, dataType.from(0));
                }
                // Check which parallelograms are actually used.
                for (int i = 0; i < numParallelograms; ++i) {
                    int context = numParallelograms - 1;
                    int pos = isCreaseEdgePos.get(context);
                    isCreaseEdgePos.set(context, pos + 1);
                    if (isCreaseEdge.get(context).size() <= pos) {
                        return Status.ioError("Index out of bounds");
                    }
                    boolean isCrease = isCreaseEdge.get(context).get(pos);
                    if (!isCrease) {
                        ++numUsedParallelograms;
                        for (int j = 0; j < numComponents; ++j) {
                            multiPredVals.set(j, dataType.add(multiPredVals.get(j), predVals.get(i).get(j)));
                        }
                    }
                }
            }
            int dstOffset = p * numComponents;
            if (numUsedParallelograms == 0) {
                // No parallelogram was valid.
                // We use the last decoded point as a reference.
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeOriginalValue(
                        outData.add(srcOffset), inCorr.add(dstOffset), outData.add(dstOffset));
            } else {
                // Compute the correction from the predicted value.
                for (int c = 0; c < numComponents; ++c) {
                    multiPredVals.set(c, dataType.div(multiPredVals.get(c), numUsedParallelograms));
                }
                this.getTransform().computeOriginalValue(
                        multiPredVals.getPointer(), inCorr.add(dstOffset), outData.add(dstOffset));
            }
        }
        return Status.ok();
    }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        if(buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            // Decode prediction mode.
            Pointer<UByte> modeRef = Pointer.newUByte();
            if(buffer.decode(modeRef).isError(chain)) return chain.get();
            UByte mode = modeRef.get();
            if(!mode.equals(MPSchemeConstrainedMultiParallelogram.OPTIMAL_MULTI_PARALLELOGRAM)) {
                // Unsupported mode.
                return Status.ioError("Unsupported mode");
            }
        }

        // Encode selected edges using separate rans bit coder for each context.
        for (int i = 0; i < MPSchemeConstrainedMultiParallelogram.MAX_NUM_PARALLELOGRAMS; ++i) {
            Pointer<UInt> numFlagsRef = Pointer.newUInt();
            if(buffer.decodeVarint(numFlagsRef).isError(chain)) return chain.get();
            int numFlags = numFlagsRef.get().intValue();
            if (numFlags > this.getMeshData().getCornerTable().getNumCorners()) {
                return Status.ioError("numFlags > cornerTable.getNumCorners()");
            }
            if (numFlags > 0) {
                isCreaseEdge.get(i).resize(numFlags);
                RAnsBitDecoder decoder = new RAnsBitDecoder();
                if (decoder.startDecoding(buffer).isError(chain)) return chain.get();
                for (int j = 0; j < numFlags; ++j) {
                    isCreaseEdge.get(i).set(j, decoder.decodeNextBit());
                }
                decoder.endDecoding();
            }
        }
        return super.decodePredictionData(buffer);
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_CONSTRAINED_MULTI_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }
}
