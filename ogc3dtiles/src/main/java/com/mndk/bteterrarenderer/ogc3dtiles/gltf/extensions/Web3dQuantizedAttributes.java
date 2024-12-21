package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import lombok.Data;

@Data
@JsonDeserialize
@GltfExtension("WEB3D_quantized_attributes")
public class Web3dQuantizedAttributes {
    private final Matrix4f decodeMatrix;
    private final Cartesian3f decodedMin;
    private final Cartesian3f decodedMax;

    @JsonCreator
    public Web3dQuantizedAttributes(@JsonProperty(value = "decodeMatrix") Matrix4f decodeMatrix,
                                    @JsonProperty(value = "decodedMin") Cartesian3f decodedMin,
                                    @JsonProperty(value = "decodedMax") Cartesian3f decodedMax) {
        this.decodeMatrix = decodeMatrix;
        this.decodedMin = decodedMin;
        this.decodedMax = decodedMax;
    }
}
