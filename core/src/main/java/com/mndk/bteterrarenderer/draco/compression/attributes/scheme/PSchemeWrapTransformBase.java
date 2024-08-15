package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PSchemeWrapTransformBase<DataT> {

    private final DataNumberType<DataT> dataType;
    private int numComponents;
    private DataT minValue;
    private DataT maxValue;
    private DataT maxDif;
    private DataT minCorrection;
    private DataT maxCorrection;
    /* This is in fact just a tmp variable to avoid reallocation. */
    private CppVector<DataT> clampedValue;

    public PSchemeWrapTransformBase(DataNumberType<DataT> dataType) {
        this.dataType = dataType;
        this.numComponents = 0;
        this.minValue = dataType.from(0);
        this.maxValue = dataType.from(0);
        this.maxDif = dataType.from(0);
        this.minCorrection = dataType.from(0);
        this.maxCorrection = dataType.from(0);
        this.clampedValue = new CppVector<>(dataType);
    }

    public PredictionSchemeTransformType getType() {
        return PredictionSchemeTransformType.WRAP;
    }

    public void init(int numComponents) {
        this.numComponents = numComponents;
        this.clampedValue.resize(numComponents);
    }

    public boolean areCorrectionsPositive() {
        return false;
    }

    public Pointer<DataT> clampPredictedValue(Pointer<DataT> predictedVal) {
        for (int i = 0; i < numComponents; ++i) {
            if (dataType.gt(predictedVal.get(i), maxValue)) {
                clampedValue.set(i, maxValue);
            } else if (dataType.lt(predictedVal.get(i), minValue)) {
                clampedValue.set(i, minValue);
            } else {
                clampedValue.set(i, predictedVal.get(i));
            }
        }
        return clampedValue.getPointer();
    }

    protected Status initCorrectionBounds() {
        long dif = dataType.toLong(dataType.sub(maxValue, minValue));
        if (dif < 0 || dif >= dataType.toLong(dataType.max())) {
            return Status.invalidParameter("Invalid difference between min and max values");
        }
        maxDif = dataType.add(dataType.from(1), dataType.from(dif));
        maxCorrection = dataType.div(maxDif, dataType.from(2));
        minCorrection = dataType.negate(maxCorrection);
        if (dataType.equals(dataType.and(maxDif, 1), 0)) {
            maxCorrection = dataType.sub(maxCorrection, dataType.from(1));
        }
        return Status.ok();
    }

}
