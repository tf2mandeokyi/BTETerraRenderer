package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.Getter;

@Getter
public class PSchemeWrapDecodingTransform<DataT, CorrT>
        extends PSchemeWrapTransformBase<DataT>
        implements PSchemeDecodingTransform<DataT, CorrT> {

    private final DataNumberType<CorrT> corrType;

    public PSchemeWrapDecodingTransform(DataNumberType<DataT> dataType, DataNumberType<CorrT> corrType) {
        super(dataType);
        this.corrType = corrType;
    }

    @Override
    public void computeOriginalValue(Pointer<DataT> predVals, Pointer<CorrT> corrVals, Pointer<DataT> outOrigVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        if(!dataType.equals(corrType)) {
            throw new IllegalArgumentException("Predictions and corrections must have the same type.");
        }

        // The only valid implementation right now is for int32_t.
        // ...bruh
        if(!dataType.equals(DataType.int32())) {
            throw new IllegalArgumentException("Only int32_t is supported for predicted values.");
        }

        predVals = this.clampPredictedValue(predVals);

        // Perform the wrapping using unsigned coordinates to avoid potential signed
        // integer overflows caused by malformed input.
        Pointer<UInt> uintPredictedVals = predVals.asRawToUInt();
        Pointer<UInt> uintCorrVals = corrVals.asRawToUInt();
        for (int i = 0; i < this.getNumComponents(); ++i) {
            DataT outOriginalVal = dataType.from(uintPredictedVals.get(i).add(uintCorrVals.get(i)));
            if (dataType.gt(outOriginalVal, this.getMaxValue())) {
                outOriginalVal = dataType.sub(outOriginalVal, this.getMaxDif());
            } else if (dataType.lt(outOriginalVal, this.getMinValue())) {
                outOriginalVal = dataType.add(outOriginalVal, this.getMaxDif());
            }
            outOrigVals.set(i, outOriginalVal);
        }
    }

    @Override
    public Status decodeTransformData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DataNumberType<DataT> dataType = this.getDataType();
        Pointer<DataT> minValue = dataType.newOwned();
        Pointer<DataT> maxValue = dataType.newOwned();
        if(buffer.decode(minValue).isError(chain)) return chain.get();
        if(buffer.decode(maxValue).isError(chain)) return chain.get();
        if(dataType.gt(minValue.get(), maxValue.get())) {
            return Status.ioError("Min value is greater than max value");
        }
        this.setMinValue(minValue.get());
        this.setMaxValue(maxValue.get());
        return this.initCorrectionBounds();
    }
}
