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

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.compression.config.NormalPredictionMode;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import com.mndk.bteterrarenderer.draco.mesh.VertexCornersIterator;

public class MPSchemeGeometricNormalPredictorArea<DataT> extends MPSchemeGeometricNormalPredictorBase<DataT> {

    public MPSchemeGeometricNormalPredictorArea(DataNumberType<DataT> dataType, MPSchemeData<?> meshData) {
        super(dataType, meshData);
        this.setNormalPredictionMode(NormalPredictionMode.TRIANGLE_AREA);
    }

    @Override
    protected void computePredictedValue(CornerIndex cornerId, Pointer<DataT> prediction) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Geometric normal predictor is not initialized");
        }
        ICornerTable cornerTable = this.getMeshData().getCornerTable();
        // Going to compute the predicted normal from the surrounding triangles
        // according to the connectivity of the given corner table.
        // Position of central vertex does not change in loop.
        VectorD.D3<Long> posCent = this.getPositionForCorner(cornerId);
        // Computing normals for triangles and adding them up.

        VectorD.D3<Long> normal = VectorD.long3();
        CornerIndex cNext, cPrev;
        for(CornerIndex corner : VertexCornersIterator.iterable(cornerTable, cornerId)) {
            // Getting corners.
            if(this.getNormalPredictionMode() == NormalPredictionMode.ONE_TRIANGLE) {
                cNext = cornerTable.next(cornerId);
                cPrev = cornerTable.previous(cornerId);
            } else {
                cNext = cornerTable.next(corner);
                cPrev = cornerTable.previous(corner);
            }
            VectorD.D3<Long> posNext = this.getPositionForCorner(cNext);
            VectorD.D3<Long> posPrev = this.getPositionForCorner(cPrev);

            // Computing delta vectors to next and prev.
            VectorD.D3<Long> deltaNext = posNext.subtract(posCent);
            VectorD.D3<Long> deltaPrev = posPrev.subtract(posCent);

            // Computing cross product.
            VectorD.D3<Long> cross = VectorD.crossProduct(deltaNext, deltaPrev);

            normal = normal.add(cross);
        }

        // Convert to int, make sure entries are not too large.
        long upperBound = 1 << 29;
        if(this.getNormalPredictionMode() == NormalPredictionMode.ONE_TRIANGLE) {
            int absSum = normal.absSum().intValue();
            if(absSum > upperBound) {
                long quotient = absSum / upperBound;
                normal = normal.divide(quotient);
            }
        } else {
            long absSum = normal.absSum();
            if(absSum > upperBound) {
                long quotient = absSum / upperBound;
                normal = normal.divide(quotient);
            }
        }

        // if(normal.absSum() > upperBound) {
        //     throw new IllegalStateException("Normal vector is too large");
        // }
        prediction.set(0, this.getDataType().from(normal.get(0)));
        prediction.set(1, this.getDataType().from(normal.get(1)));
        prediction.set(2, this.getDataType().from(normal.get(2)));
    }

    @Override
    public Status setNormalPredictionMode(NormalPredictionMode mode) {
        if(mode != NormalPredictionMode.ONE_TRIANGLE && mode != NormalPredictionMode.TRIANGLE_AREA) {
            return Status.invalidParameter("Invalid normal prediction mode");
        }
        this.normalPredictionMode = mode;
        return Status.ok();
    }
}
