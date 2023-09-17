package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@GltfExtension("KHR_draco_mesh_compression")
public class KHRDracoMeshCompression {
    private final int bufferView;
    private final Map<String, Integer> attributes;

    @JsonCreator
    public KHRDracoMeshCompression(@JsonProperty("bufferView") int bufferView,
                                   @JsonProperty("attributes") Map<String, Integer> attributes) {
        this.bufferView = bufferView;
        this.attributes = attributes;
    }
}
