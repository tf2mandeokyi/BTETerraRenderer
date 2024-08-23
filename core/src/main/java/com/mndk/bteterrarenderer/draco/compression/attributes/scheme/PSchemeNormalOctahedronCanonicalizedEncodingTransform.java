package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.VectorD;

public class PSchemeNormalOctahedronCanonicalizedEncodingTransform<DataT>
        extends PSchemeNormalOctahedronCanonicalizedTransformBase<DataT>
        implements PSchemeEncodingTransform<DataT, DataT> {

    public PSchemeNormalOctahedronCanonicalizedEncodingTransform(DataNumberType<DataT> dataType,
                                                                 DataT maxQuantizedValue) {
        super(dataType, maxQuantizedValue);
    }

    @Override public DataNumberType<DataT> getCorrType() { return this.getDataType(); }
    @Override public void init(Pointer<DataT> origData, int size, int numComponents) {}

    @Override
    public Status encodeTransformData(EncoderBuffer buffer) {
        buffer.encode(DataType.int32(), this.getDataType().toInt(this.getMaxQuantizedValue()));
        buffer.encode(DataType.int32(), this.getDataType().toInt(this.getCenterValue()));
        return Status.ok();
    }

    @Override
    public void computeCorrection(Pointer<DataT> origVals, Pointer<DataT> predVals, Pointer<DataT> outCorrVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        if(dataType.gt(predVals.get(0), dataType.mul(this.getCenterValue(), 2))) {
            throw new IllegalStateException("Predicted value is greater than 2 * center value");
        }
        if(dataType.gt(predVals.get(1), dataType.mul(this.getCenterValue(), 2))) {
            throw new IllegalStateException("Predicted value is greater than 2 * center value");
        }
        if(dataType.gt(origVals.get(0), dataType.mul(this.getCenterValue(), 2))) {
            throw new IllegalStateException("Original value is greater than 2 * center value");
        }
        if(dataType.gt(origVals.get(1), dataType.mul(this.getCenterValue(), 2))) {
            throw new IllegalStateException("Original value is greater than 2 * center value");
        }
        if(dataType.gt(0, predVals.get(0))) {
            throw new IllegalStateException("Predicted value is less than 0");
        }
        if(dataType.gt(0, predVals.get(1))) {
            throw new IllegalStateException("Predicted value is less than 0");
        }
        if(dataType.gt(0, origVals.get(0))) {
            throw new IllegalStateException("Original value is less than 0");
        }
        if(dataType.gt(0, origVals.get(1))) {
            throw new IllegalStateException("Original value is less than 0");
        }

        VectorD.D2<DataT> orig = new VectorD.D2<>(dataType, origVals.get(0), origVals.get(1));
        VectorD.D2<DataT> pred = new VectorD.D2<>(dataType, predVals.get(0), predVals.get(1));
        VectorD.D2<DataT> corr = computeCorrection(orig, pred);

        outCorrVals.set(0, corr.get(0));
        outCorrVals.set(1, corr.get(1));
    }

    private VectorD.D2<DataT> computeCorrection(VectorD.D2<DataT> orig, VectorD.D2<DataT> pred) {
        DataNumberType<DataT> dataType = this.getDataType();
        VectorD.D2<DataT> t = new VectorD.D2<>(dataType, this.getCenterValue(), this.getCenterValue());
        orig = orig.subtract(t);
        pred = pred.subtract(t);
        if(!this.isInDiamond(pred.get(0), pred.get(1))) {
            this.invertDiamond(pred.getPointer(0), pred.getPointer(1));
            this.invertDiamond(orig.getPointer(0), orig.getPointer(1));
        }
        if(!this.isInBottomLeft(pred)) {
            int rotationCount = this.getRotationCount(pred);
            orig = this.rotatePoint(orig, rotationCount);
            pred = this.rotatePoint(pred, rotationCount);
        }
        VectorD.D2<DataT> corr = orig.subtract(pred);
        corr.set(0, this.makePositive(corr.get(0)));
        corr.set(1, this.makePositive(corr.get(1)));
        return corr;
    }
}
