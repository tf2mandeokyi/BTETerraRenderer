package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
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

    protected VectorD.L3 getPositionForDataId(int dataId) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        PointIndex pointId = entryToPointIdMap.get(dataId);
        AttributeValueIndex posValId = positionAttribute.getMappedIndex(pointId);
        VectorD.L3 pos = new VectorD.L3();
        positionAttribute.convertValue(posValId, pos.getPointer());
        return pos;
    }

    protected VectorD.L3 getPositionForCorner(CornerIndex ci) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        int vertId = meshData.getCornerTable().getVertex(ci).getValue();
        int dataId = meshData.getVertexToDataMap().get(vertId);
        return getPositionForDataId(dataId);
    }

    protected VectorD.I2 getOctahedralCoordForDataId(int dataId, Pointer<DataT> data) {
        if(!this.isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
        int dataOffset = dataId * 2;
        return new VectorD.I2(dataType.toInt(data.get(dataOffset)), dataType.toInt(data.get(dataOffset + 1)));
    }

    protected abstract void computePredictedValue(CornerIndex cornerId, Pointer<DataT> prediction);
}
