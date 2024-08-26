package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeDecodingTransform<DataT, CorrT> {

    void init(int numComponents);

    DataNumberType<DataT> getDataType();
    DataNumberType<CorrT> getCorrType();

    default void computeOriginalValue(Pointer<DataT> predVals, Pointer<CorrT> corrVals,
                                      Pointer<DataT> outOrigVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        DataNumberType<CorrT> corrType = this.getCorrType();
        if(!dataType.equals(corrType)) {
            throw new IllegalArgumentException("For the default prediction transform, correction and input " +
                    "data must be of the same type.");
        }
        for (int i = 0; i < getNumComponents(); ++i) {
            outOrigVals.set(i, BTRUtil.<DataT>uncheckedCast(DataType.add(dataType, predVals.get(i), corrType, corrVals.get(i))));
        }
    }

    default Status decodeTransformData(DecoderBuffer buffer) { return Status.ok(); }
    default boolean areCorrectionsPositive() { return false; }
    PredictionSchemeTransformType getType();
    default int getNumComponents() {
        throw new UnsupportedOperationException("This transform does not support number of components");
    }
    default int getQuantizationBits() {
        throw new UnsupportedOperationException("This transform does not support quantization bits");
    }
}
