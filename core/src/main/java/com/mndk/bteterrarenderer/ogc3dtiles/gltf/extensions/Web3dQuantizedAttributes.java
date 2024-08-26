package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;

@Data
@JsonDeserialize
@GltfExtension("WEB3D_quantized_attributes")
public class Web3dQuantizedAttributes {
    private final Matrix4 decodeMatrix;
    private final Cartesian3 decodedMin;
    private final Cartesian3 decodedMax;

    @JsonCreator
    public Web3dQuantizedAttributes(@JsonProperty(value = "decodeMatrix") Matrix4 decodeMatrix,
                                    @JsonProperty(value = "decodedMin") Cartesian3 decodedMin,
                                    @JsonProperty(value = "decodedMax") Cartesian3 decodedMax) {
        this.decodeMatrix = decodeMatrix;
        this.decodedMin = decodedMin;
        this.decodedMax = decodedMax;
    }
}
