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
import com.mndk.bteterrarenderer.draco.compression.attributes.OctahedronToolBox;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.BitUtils;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;

public class PSchemeNormalOctahedronTransformBase<DataT> {

    @Getter
    private final DataNumberType<DataT> dataType;
    private final OctahedronToolBox octahedronToolBox = new OctahedronToolBox();

    public PSchemeNormalOctahedronTransformBase(DataNumberType<DataT> dataType) {
        this.dataType = dataType;
    }

    public PSchemeNormalOctahedronTransformBase(DataNumberType<DataT> dataType, DataT maxQuantizedValue) {
        this.dataType = dataType;
        setMaxQuantizedValue(maxQuantizedValue);
    }

    public PredictionSchemeTransformType getType() {
        return PredictionSchemeTransformType.NORMAL_OCTAHEDRON;
    }

    public boolean areCorrectionsPositive() {
        return true;
    }

    public DataT getMaxQuantizedValue() {
        return dataType.from(octahedronToolBox.getMaxQuantizedValue());
    }

    public DataT getCenterValue() {
        return dataType.from(octahedronToolBox.getCenterValue());
    }

    public int getQuantizationBits() {
        return octahedronToolBox.getQuantizationBits();
    }

    protected Status setMaxQuantizedValue(DataT maxQuantizedValue) {
        if(dataType.equals(dataType.mod(maxQuantizedValue, 2), 0)) {
            return Status.dracoError("Max quantized value must be of the form 2^b-1");
        }
        int q = BitUtils.mostSignificantBit(dataType, maxQuantizedValue) + 1;
        return octahedronToolBox.setQuantizationBits(q);
    }

    protected boolean isInDiamond(DataT s, DataT t) {
        return octahedronToolBox.isInDiamond(dataType.toInt(s), dataType.toInt(t));
    }

    protected void invertDiamond(Pointer<DataT> s, Pointer<DataT> t) {
        octahedronToolBox.invertDiamond(s.asRawToInt(), t.asRawToInt());
    }

    protected DataT modMax(DataT x) {
        return dataType.from(octahedronToolBox.modMax(dataType.toInt(x)));
    }

    protected DataT makePositive(DataT x) {
        return dataType.from(octahedronToolBox.makePositive(dataType.toInt(x)));
    }
}
