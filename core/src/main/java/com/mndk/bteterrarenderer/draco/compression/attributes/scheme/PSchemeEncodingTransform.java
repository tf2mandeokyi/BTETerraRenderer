package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PSchemeEncodingTransform<DataT, CorrT> {

    void init(Pointer<DataT> origData, int size, int numComponents);

    DataNumberType<DataT> getDataType();
    DataNumberType<CorrT> getCorrType();

    default void computeCorrection(Pointer<DataT> origVals, Pointer<DataT> predVals, Pointer<CorrT> outCorrVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        DataNumberType<CorrT> corrType = this.getCorrType();
        if(!origVals.getType().equals(outCorrVals.getType())) {
            throw new IllegalArgumentException(
                    "For the default prediction transform, correction and input " +
                    "data must be of the same type.");
        }
        for (int i = 0; i < getNumComponents(); ++i) {
            outCorrVals.set(i, corrType.from(dataType, dataType.sub(origVals.get(i), predVals.get(i))));
        }
    }

    default Status encodeTransformData(EncoderBuffer buffer) { return Status.ok(); }
    default boolean areCorrectionsPositive() { return false; }
    default PredictionSchemeTransformType getType() { return PredictionSchemeTransformType.DELTA; }
    default int getNumComponents() {
        throw new UnsupportedOperationException("This transform does not support number of components");
    }
    default int getQuantizationBits() {
        throw new UnsupportedOperationException("This transform does not support quantization bits");
    }
}
