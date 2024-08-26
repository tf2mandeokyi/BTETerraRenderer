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

public class PSchemeDeltaEncoder<DataT, CorrT> extends PSchemeEncoder<DataT, CorrT> {

    public PSchemeDeltaEncoder(PointAttribute attribute,
                               PSchemeEncodingTransform<DataT, CorrT> transform) {
        super(attribute, transform);
    }

    @Override
    public Status computeCorrectionValues(Pointer<DataT> inData, Pointer<CorrT> outCorr, int size, int numComponents,
                                          Pointer<PointIndex> entryToPointIdMap) {
        this.getTransform().init(inData, size, numComponents);
        for (int i = size - numComponents; i > 0; i -= numComponents) {
            this.getTransform().computeCorrection(inData.add(i), inData.add(i - numComponents), outCorr.add(i));
        }
        // Encode correction for the first element.
        Pointer<DataT> zeroVals = this.getDataType().newArray(numComponents);
        this.getTransform().computeCorrection(inData, zeroVals, outCorr);
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
