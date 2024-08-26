package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;

@Getter
public class PSchemeWrapEncodingTransform<DataT, CorrT>
        extends PSchemeWrapTransformBase<DataT>
        implements PSchemeEncodingTransform<DataT, CorrT> {

    private final DataNumberType<CorrT> corrType;

    public PSchemeWrapEncodingTransform(DataNumberType<DataT> dataType, DataNumberType<CorrT> corrType) {
        super(dataType);
        this.corrType = corrType;
    }

    public void init(Pointer<DataT> origData, int size, int numComponents) {
        super.init(numComponents);
        if (size == 0) {
            return;
        }
        DataT minValue = origData.get(0);
        DataT maxValue = minValue;
        for (int i = 1; i < size; ++i) {
            DataT value = origData.get(i);
            if (this.getDataType().compareTo(value, minValue) < 0) {
                minValue = value;
            } else if (this.getDataType().compareTo(value, maxValue) > 0) {
                maxValue = value;
            }
        }
        this.setMinValue(minValue);
        this.setMaxValue(maxValue);
        this.initCorrectionBounds();
    }

    public void computeCorrection(Pointer<DataT> origVals, Pointer<DataT> predVals, Pointer<CorrT> outCorrVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        for (int i = 0; i < this.getNumComponents(); ++i) {
            predVals = this.clampPredictedValue(predVals);
            DataT corrVal = dataType.sub(origVals.get(i), predVals.get(i));
            if (dataType.compareTo(corrVal, this.getMinCorrection()) < 0) {
                corrVal = dataType.add(corrVal, this.getMaxDif());
            } else if (dataType.compareTo(corrVal, this.getMaxCorrection()) > 0) {
                corrVal = dataType.sub(corrVal, this.getMaxDif());
            }
            outCorrVals.set(i, this.corrType.from(dataType, corrVal));
        }
    }

    public Status encodeTransformData(EncoderBuffer buffer) {
        buffer.encode(this.getDataType(), this.getMinValue());
        buffer.encode(this.getDataType(), this.getMaxValue());
        return Status.ok();
    }
}
