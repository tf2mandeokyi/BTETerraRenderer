package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.joml.Vector3d;

@Data
@GltfExtension("CESIUM_RTC")
public class CesiumRTC {
    private final Vector3d center;

    @JsonCreator
    public CesiumRTC(@JsonProperty(value = "center") double[] center) {
        this.center = new Vector3d(center);
    }
}
