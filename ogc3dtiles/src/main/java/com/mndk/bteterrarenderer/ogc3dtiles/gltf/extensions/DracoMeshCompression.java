package com.mndk.bteterrarenderer.ogc3dtiles.gltf.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
@JsonDeserialize
@GltfExtension("KHR_draco_mesh_compression")
public class DracoMeshCompression {

    private final int bufferView;
    private final Map<String, Integer> attributes;

    @JsonCreator
    public DracoMeshCompression(@JsonProperty(value = "bufferView") int bufferView,
                                @JsonProperty(value = "attributes") Map<String, Integer> attributes) {
        this.bufferView = bufferView;
        this.attributes = attributes;
    }

}
