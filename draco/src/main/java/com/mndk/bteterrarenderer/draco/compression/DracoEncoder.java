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

package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptionsBase;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Options;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

@SuppressWarnings("unused")
public class DracoEncoder extends DracoEncoderBase<EncoderOptionsBase<GeometryAttribute.Type>> {

    public Status encodePointCloudToBuffer(PointCloud pc, EncoderBuffer outBuffer) {
        DracoExpertEncoder encoder = new DracoExpertEncoder(pc);
        encoder.reset(this.createExpertEncoderOptions(pc));
        return encoder.encodeToBuffer(outBuffer);
    }

    public Status encodeMeshToBuffer(Mesh m, EncoderBuffer outBuffer) {
        DracoExpertEncoder encoder = new DracoExpertEncoder(m);
        encoder.reset(this.createExpertEncoderOptions(m));
        Status status = encoder.encodeToBuffer(outBuffer);
        if (status.isError()) return status;

        this.setNumEncodedPoints(encoder.getNumEncodedPoints());
        this.setNumEncodedFaces(encoder.getNumEncodedFaces());
        return status;
    }

    public void reset(EncoderOptionsBase<GeometryAttribute.Type> options) {
        super.reset(options);
    }

    public void reset() {
        super.reset();
    }

    public void setSpeedOptions(int encodingSpeed, int decodingSpeed) {
        super.setSpeedOptions(encodingSpeed, decodingSpeed);
    }

    public void setAttributeQuantization(GeometryAttribute.Type type, int quantizationBits) {
        this.getOptions().setAttributeInt(type, "quantization_bits", quantizationBits);
    }

    public void setAttributeExplicitQuantization(GeometryAttribute.Type type, int quantizationBits, int numDims,
                                                 Pointer<Float> origin, float range) {
        this.getOptions().setAttributeInt(type, "quantization_bits", quantizationBits);
        this.getOptions().setAttributeVector(type, "quantization_origin", numDims, origin);
        this.getOptions().setAttributeFloat(type, "quantization_range", range);
    }

    public Status setAttributePredictionScheme(GeometryAttribute.Type type, int predictionSchemeMethod) {
        Status status = super.checkPredictionScheme(type, PredictionSchemeMethod.valueOf(predictionSchemeMethod));
        if (status.isError()) return status;
        this.getOptions().setAttributeInt(type, "prediction_scheme", predictionSchemeMethod);
        return status;
    }

    public void setEncodingMethod(MeshEncoderMethod encodingMethod) {
        super.setEncodingMethod(encodingMethod);
    }

    public EncoderOptions createExpertEncoderOptions(PointCloud pc) {
        EncoderOptions retOptions = EncoderOptions.createEmptyOptions();
        retOptions.setGlobalOptions(this.getOptions().getGlobalOptions());
        retOptions.setFeatureOptions(this.getOptions().getFeatureOptions());
        for (int i = 0; i < pc.getNumAttributes(); ++i) {
            Options attOptions = this.getOptions().findAttributeOptions(pc.getAttribute(i).getAttributeType());
            if (attOptions != null) {
                retOptions.setAttributeOptions(i, attOptions);
            }
        }
        return retOptions;
    }

    @Override protected EncoderOptionsBase<GeometryAttribute.Type> createDefaultOptions() {
        return EncoderOptionsBase.createDefaultOptions();
    }
}
