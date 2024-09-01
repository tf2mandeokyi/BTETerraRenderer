package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3f;
import lombok.Data;

@Data
@GltfExtension("CESIUM_RTC")
public class CesiumRTC {
    private final Cartesian3f center;

    @JsonCreator
    public CesiumRTC(@JsonProperty(value = "center") Cartesian3f center) {
        this.center = center;
    }
}
