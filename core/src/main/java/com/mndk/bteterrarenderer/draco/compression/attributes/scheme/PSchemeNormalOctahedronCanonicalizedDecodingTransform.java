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
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.VectorD;

public class PSchemeNormalOctahedronCanonicalizedDecodingTransform<DataT>
        extends PSchemeNormalOctahedronCanonicalizedTransformBase<DataT>
        implements PSchemeDecodingTransform<DataT, DataT> {

    public PSchemeNormalOctahedronCanonicalizedDecodingTransform(DataNumberType<DataT> dataType) {
        super(dataType);
    }

    @Override public DataNumberType<DataT> getCorrType() { return this.getDataType(); }
    @Override public void init(int numComponents) {}

    @Override
    public Status decodeTransformData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DataNumberType<DataT> dataType = this.getDataType();
        Pointer<DataT> maxQuantizedValue = dataType.newOwned();
        Pointer<DataT> centerValue = dataType.newOwned();
        if(buffer.decode(maxQuantizedValue).isError(chain)) return chain.get();
        if(buffer.decode(centerValue).isError(chain)) return chain.get();
        if(this.setMaxQuantizedValue(maxQuantizedValue.get()).isError(chain)) return chain.get();
        // Account for reading wrong values, e.g., due to fuzzing.
        if(this.getQuantizationBits() < 2) {
            return Status.dracoError("Quantization bits must be at least 2");
        }
        if(this.getQuantizationBits() > 30) {
            return Status.dracoError("Quantization bits must be at most 30");
        }
        return Status.ok();
    }

    @Override
    public void computeOriginalValue(Pointer<DataT> predVals, Pointer<DataT> corrVals, Pointer<DataT> outOrigVals) {
        DataNumberType<DataT> dataType = this.getDataType();
        if(dataType.gt(predVals.get(0), dataType.mul(this.getCenterValue() , 2))) {
            throw new IllegalArgumentException("Predicted value must be less than or equal to 2 * center value");
        }
        if(dataType.gt(predVals.get(1), dataType.mul(this.getCenterValue() , 2))) {
            throw new IllegalArgumentException("Predicted value must be less than or equal to 2 * center value");
        }
        if(dataType.gt(corrVals.get(0), dataType.mul(this.getCenterValue() , 2))) {
            throw new IllegalArgumentException("Correction value must be less than or equal to 2 * center value");
        }
        if(dataType.gt(corrVals.get(1), dataType.mul(this.getCenterValue() , 2))) {
            throw new IllegalArgumentException("Correction value must be less than or equal to 2 * center value");
        }

        if(dataType.gt(0, predVals.get(0))) {
            throw new IllegalArgumentException("Predicted value must be greater than or equal to 0");
        }
        if(dataType.gt(0, predVals.get(1))) {
            throw new IllegalArgumentException("Predicted value must be greater than or equal to 0");
        }
        if(dataType.gt(0, corrVals.get(0))) {
            throw new IllegalArgumentException("Correction value must be greater than or equal to 0");
        }
        if(dataType.gt(0, corrVals.get(1))) {
            throw new IllegalArgumentException("Correction value must be greater than or equal to 0");
        }

        VectorD.D2<DataT> pred = new VectorD.D2<>(dataType, predVals.get(0), predVals.get(1));
        VectorD.D2<DataT> corr = new VectorD.D2<>(dataType, corrVals.get(0), corrVals.get(1));
        VectorD.D2<DataT> orig = computeOriginalValue(pred, corr);

        outOrigVals.set(0, orig.get(0));
        outOrigVals.set(1, orig.get(1));
    }

    private VectorD.D2<DataT> computeOriginalValue(VectorD.D2<DataT> pred, VectorD.D2<DataT> corr) {
        DataNumberType<DataT> dataType = this.getDataType();
        VectorD.D2<DataT> t = new VectorD.D2<>(dataType, this.getCenterValue(), this.getCenterValue());
        pred = pred.subtract(t);
        boolean predIsInDiamond = this.isInDiamond(pred.get(0), pred.get(1));
        if(!predIsInDiamond) {
            this.invertDiamond(pred.getPointer(0), pred.getPointer(1));
        }
        boolean predIsInBottomLeft = this.isInBottomLeft(pred);
        int rotationCount = this.getRotationCount(pred);
        if(!predIsInBottomLeft) {
            pred = this.rotatePoint(pred, rotationCount);
        }
        VectorD.D2<DataT> orig = new VectorD.D2<>(dataType,
                this.modMax(dataType.add(pred.get(0), corr.get(0))),
                this.modMax(dataType.add(pred.get(1), corr.get(1)))
        );
        if(!predIsInBottomLeft) {
            int reverseRotationCount = (4 - rotationCount) % 4;
            orig = this.rotatePoint(orig, reverseRotationCount);
        }
        if(!predIsInDiamond) {
            this.invertDiamond(orig.getPointer(0), orig.getPointer(1));
        }
        orig = orig.add(t);
        return orig;
    }
}
