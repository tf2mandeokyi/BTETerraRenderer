package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.connector.terraplusplus.JacksonConnector;
import com.mndk.bteterrarenderer.projection.YamlTileProjection;
import lombok.Data;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class ProjectionYamlLoader extends YamlLoader<Map<String, YamlTileProjection>> {

    public static final ProjectionYamlLoader INSTANCE = new ProjectionYamlLoader(
            "projections", "assets/" + BTETerraRendererCore.MODID + "/default_projections.yml"
    );

    public ProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath);
    }

    @Override
    protected Map<String, YamlTileProjection> load(String fileName, Reader fileReader) throws IOException {
        return JacksonConnector.INSTANCE.readYamlProjectionFile(fileReader).tileProjections;
    }

    @Override
    protected void addToResult(Map<String, YamlTileProjection> originalT, Map<String, YamlTileProjection> newT) {
        originalT.putAll(newT);
    }

    @Data
    @JsonDeserialize
    public static class ProjectionYamlFile {
        @JsonProperty(value = "tile_projections", required = true)
        public Map<String, YamlTileProjection> tileProjections;
    }
}
