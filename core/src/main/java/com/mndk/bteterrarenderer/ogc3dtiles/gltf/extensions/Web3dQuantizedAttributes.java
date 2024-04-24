package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.Data;

@Data
@GltfExtension("WEB3D_quantized_attributes")
public class Web3dQuantizedAttributes {
    private final Matrix4 decodeMatrix;
    private final Cartesian3 decodedMin;
    private final Cartesian3 decodedMax;

    @JsonCreator
    public Web3dQuantizedAttributes(
            @JsonProperty("decodeMatrix") Matrix4 decodeMatrix,
            @JsonProperty("decodedMin") Cartesian3 decodedMin,
            @JsonProperty("decodedMax") Cartesian3 decodedMax
    ) {
        this.decodeMatrix = decodeMatrix;
        this.decodedMin = decodedMin;
        this.decodedMax = decodedMax;
    }
}
