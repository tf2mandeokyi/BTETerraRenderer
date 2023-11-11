package com.mndk.bteterrarenderer.core.loader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileProjectionImpl;
import lombok.Data;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class FlatTileProjectionYamlLoader extends YamlLoader<Map<String, FlatTileProjectionImpl>> {

    public static final FlatTileProjectionYamlLoader INSTANCE = new FlatTileProjectionYamlLoader(
            "projections", "assets/" + BTETerraRendererConstants.MODID + "/default_projections.yml"
    );

    public FlatTileProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath);
    }

    @Override
    protected Map<String, FlatTileProjectionImpl> load(String fileName, Reader fileReader) throws IOException {
        return BTETerraRendererConstants.YAML_MAPPER.readValue(fileReader, new TypeReference<ProjectionYamlFile>() {})
                .tileProjections;
    }

    @Override
    protected void addToResult(Map<String, FlatTileProjectionImpl> originalT, Map<String, FlatTileProjectionImpl> newT) {
        originalT.putAll(newT);
    }

    @Data
    @JsonDeserialize
    public static class ProjectionYamlFile {
        public final Map<String, FlatTileProjectionImpl> tileProjections;
        @JsonCreator
        public ProjectionYamlFile(
                @JsonProperty(value = "tile_projections", required = true)
                Map<String, FlatTileProjectionImpl> tileProjections
        ) {
            this.tileProjections = tileProjections;
        }
    }
}
