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
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.NormalPredictionMode;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;
import lombok.Getter;
import lombok.Setter;

public abstract class MPSchemeGeometricNormalPredictorBase<DataT> {

    @Getter
    protected final DataNumberType<DataT> dataType;
    @Setter
    protected PointAttribute positionAttribute = null;
    @Setter
    protected Pointer<PointIndex> entryToPointIdMap = Pointer.nullPointer();
    @Getter
    protected final MPSchemeData<?> meshData;
    @Getter
    protected NormalPredictionMode normalPredictionMode;

    protected MPSchemeGeometricNormalPredictorBase(DataNumberType<DataT> dataType, MPSchemeData<?> meshData) {
        this.dataType = dataType;
        this.meshData = meshData;
    }

    public boolean isInitialized() {
        return positionAttribute != null && entryToPointIdMap != null;
    }

    public abstract Status setNormalPredictionMode(NormalPredictionMode mode);

    protected VectorD.D3<Long> getPositionForDataId(int dataId) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        PointIndex pointId = entryToPointIdMap.get(dataId);
        AttributeValueIndex posValId = positionAttribute.getMappedIndex(pointId);
        VectorD.D3<Long> pos = VectorD.long3();
        positionAttribute.convertValue(posValId, pos.getPointer());
        return pos;
    }

    protected VectorD.D3<Long> getPositionForCorner(CornerIndex ci) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        int vertId = meshData.getCornerTable().getVertex(ci).getValue();
        int dataId = meshData.getVertexToDataMap().get(vertId);
        return getPositionForDataId(dataId);
    }

    protected VectorD.D2<Integer> getOctahedralCoordForDataId(int dataId, Pointer<DataT> data) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        int dataOffset = dataId * 2;
        return VectorD.int2(dataType.toInt(data.get(dataOffset)), dataType.toInt(data.get(dataOffset + 1)));
    }

    protected abstract void computePredictedValue(CornerIndex cornerId, Pointer<DataT> prediction);
}
