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

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;

public class MPSchemeParallelogramEncoder<DataT, CorrT> extends MPSchemeEncoder<DataT, CorrT> {

    public MPSchemeParallelogramEncoder(PointAttribute attribute,
                                        PSchemeEncodingTransform<DataT, CorrT> transform,
                                        MPSchemeData<?> meshData) {
        super(attribute, transform, meshData);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(inData, size, numComponents);
        // For storage of prediction values
        Pointer<DataT> predVals = this.getDataType().newArray(numComponents);

        // We start processing from the end because this prediction uses data from
        // previous entries that could be overwritten when an entry is processed.
        ICornerTable table = this.getMeshData().getCornerTable();
        CppVector<Integer> vertexToDataMap = this.getMeshData().getVertexToDataMap();
        for(int p = (int) (this.getMeshData().getDataToCornerMap().size() - 1); p > 0; p--) {
            CornerIndex cornerId = this.getMeshData().getDataToCornerMap().get(p);
            int dstOffset = p * numComponents;
            Status status = MPSchemeParallelogram.computeParallelogramPrediction(
                    p, cornerId, table, vertexToDataMap, inData, numComponents, predVals);
            if(status.isError()) {
                int srcOffset = (p - 1) * numComponents;
                this.getTransform().computeCorrection(inData.add(dstOffset), inData.add(srcOffset), outCorr.add(dstOffset));
            }
            else {
                this.getTransform().computeCorrection(inData.add(dstOffset), predVals, outCorr.add(dstOffset));
            }
        }
        // First element is always fixed because it cannot be predicted.
        for(int i = 0; i < numComponents; i++) {
            predVals.set(i, this.getDataType().from(0));
        }
        this.getTransform().computeCorrection(inData, predVals, outCorr);
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.MESH_PARALLELOGRAM;
    }

    @Override
    public boolean isInitialized() {
        return this.getMeshData().isInitialized();
    }
}
