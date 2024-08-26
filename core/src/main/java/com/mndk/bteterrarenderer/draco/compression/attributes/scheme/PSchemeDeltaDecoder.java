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

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.Status;

public class PSchemeDeltaDecoder<DataT, CorrT> extends PSchemeDecoder<DataT, CorrT> {

    public PSchemeDeltaDecoder(PointAttribute attribute,
                               PSchemeDecodingTransform<DataT, CorrT> transform) {
        super(attribute, transform);
    }

    @Override
    public Status computeOriginalValues(Pointer<CorrT> inCorr, Pointer<DataT> outData,
                                        int size, int numComponents, Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(numComponents);
        // Decode the original value for the first element.
        Pointer<DataT> zeroVals = this.getDataType().newArray(numComponents);
        this.getTransform().computeOriginalValue(zeroVals, inCorr, outData);

        // Decode data from the front using D(i) = D(i) + D(i - 1).
        for (int i = numComponents; i < size; i += numComponents) {
            this.getTransform().computeOriginalValue(outData.add(i - numComponents),
                    inCorr.add(i), outData.add(i));
        }
        return Status.ok();
    }

    @Override
    public PredictionSchemeMethod getPredictionMethod() {
        return PredictionSchemeMethod.DIFFERENCE;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }
}
