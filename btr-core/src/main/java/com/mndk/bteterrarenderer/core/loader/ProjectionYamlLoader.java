package com.mndk.bteterrarenderer.core.loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.projection.YamlTileProjection;
import lombok.Data;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class ProjectionYamlLoader extends YamlLoader<Map<String, YamlTileProjection>> {

    public static final ProjectionYamlLoader INSTANCE = new ProjectionYamlLoader(
            "projections", "assets/" + BTETerraRendererConstants.MODID + "/default_projections.yml"
    );

    public ProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath);
    }

    @Override
    protected Map<String, YamlTileProjection> load(String fileName, Reader fileReader) throws IOException {
        return BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, new TypeReference<ProjectionYamlFile>() {})
                .tileProjections;
    }

    @Override
    protected void addToResult(Map<String, YamlTileProjection> originalT, Map<String, YamlTileProjection> newT) {
        originalT.putAll(newT);
    }

    @Data
    @JsonDeserialize
    public static class ProjectionYamlFile {
        public final Map<String, YamlTileProjection> tileProjections;
        @JsonCreator
        public ProjectionYamlFile(
                @JsonProperty(value = "tile_projections", required = true)
                Map<String, YamlTileProjection> tileProjections
        ) {
            this.tileProjections = tileProjections;
        }
    }
}
