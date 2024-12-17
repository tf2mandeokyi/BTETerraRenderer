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
        if (!origVals.getType().equals(outCorrVals.getType())) {
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
