package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.math.JOMLUtils;
import lombok.Data;
import org.joml.Matrix4d;
import org.joml.Vector3d;

@Data
@JsonDeserialize
@GltfExtension("WEB3D_quantized_attributes")
public class Web3dQuantizedAttributes {
    private final Matrix4d decodeMatrix;
    private final Vector3d decodedMin;
    private final Vector3d decodedMax;

    @JsonCreator
    public Web3dQuantizedAttributes(@JsonProperty(value = "decodeMatrix") double[] decodeMatrix,
                                    @JsonProperty(value = "decodedMin") double[] decodedMin,
                                    @JsonProperty(value = "decodedMax") double[] decodedMax) {
        this.decodeMatrix = JOMLUtils.columnMajor4d(decodeMatrix);
        this.decodedMin = new Vector3d(decodedMin);
        this.decodedMax = new Vector3d(decodedMax);
    }
}
