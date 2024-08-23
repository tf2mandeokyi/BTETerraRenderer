package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.VectorD;

public class PSchemeNormalOctahedronCanonicalizedTransformBase<DataT>
        extends PSchemeNormalOctahedronTransformBase<DataT> {

    public PSchemeNormalOctahedronCanonicalizedTransformBase(DataNumberType<DataT> dataType) {
        super(dataType);
    }

    public PSchemeNormalOctahedronCanonicalizedTransformBase(DataNumberType<DataT> dataType, DataT maxQuantizedValue) {
        super(dataType, maxQuantizedValue);
    }

    @Override
    public PredictionSchemeTransformType getType() {
        return PredictionSchemeTransformType.NORMAL_OCTAHEDRON_CANONICALIZED;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    protected int getRotationCount(VectorD.D2<DataT> pred) {
        DataT signX = pred.get(0);
        DataT signY = pred.get(1);
        DataNumberType<DataT> dataType = this.getDataType();

        int rotationCount;
        if (dataType.equals(signX, 0)) {
            if (dataType.equals(signY, 0)) {
                rotationCount = 0;
            } else if (dataType.gt(signY, 0)) {
                rotationCount = 3;
            } else {
                rotationCount = 1;
            }
        } else if (dataType.gt(signX, 0)) {
            if (dataType.ge(signY, 0)) {
                rotationCount = 2;
            } else {
                rotationCount = 1;
            }
        } else {
            if (dataType.le(signY, 0)) {
                rotationCount = 0;
            } else {
                rotationCount = 3;
            }
        }
        return rotationCount;
    }

    protected VectorD.D2<DataT> rotatePoint(VectorD.D2<DataT> p, int rotationCount) {
        DataNumberType<DataT> dataType = this.getDataType();
        switch (rotationCount) {
            case 1:
                return new VectorD.D2<>(dataType, p.get(1), dataType.negate(p.get(0)));
            case 2:
                return new VectorD.D2<>(dataType, dataType.negate(p.get(0)), dataType.negate(p.get(1)));
            case 3:
                return new VectorD.D2<>(dataType, dataType.negate(p.get(1)), p.get(0));
            default:
                return p;
        }
    }

    protected boolean isInBottomLeft(VectorD.D2<DataT> p) {
        DataNumberType<DataT> dataType = this.getDataType();
        if (dataType.equals(p.get(0), 0) && dataType.equals(p.get(1), 0)) {
            return true;
        }
        return (dataType.lt(p.get(0), 0) && dataType.le(p.get(1), 0));
    }
}
