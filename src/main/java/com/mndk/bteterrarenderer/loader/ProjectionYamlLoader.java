package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.projection.YamlTileProjection;
import com.mndk.bteterrarenderer.util.reader.TppDepJacksonYAMLReader;
import lombok.Data;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.annotation.JsonProperty;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.type.TypeReference;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class ProjectionYamlLoader extends YamlLoader<Map<String, YamlTileProjection>> {

    public static final ProjectionYamlLoader INSTANCE = new ProjectionYamlLoader(
            "projections", "assets/" + BTETerraRenderer.MODID + "/default_projections.yml"
    );

    public ProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath);
    }

    @Override
    protected Map<String, YamlTileProjection> load(String fileName, Reader fileReader) throws IOException {
        return TppDepJacksonYAMLReader.read(fileReader, new TypeReference<ProjectionYamlFile>() {}).tileProjections;
    }

    @Override
    protected void addToResult(Map<String, YamlTileProjection> originalT, Map<String, YamlTileProjection> newT) {
        originalT.putAll(newT);
    }


    @Data
    @JsonDeserialize
    static class ProjectionYamlFile {
        @JsonProperty(value = "tile_projections", required = true)
        public Map<String, YamlTileProjection> tileProjections;
    }
}
