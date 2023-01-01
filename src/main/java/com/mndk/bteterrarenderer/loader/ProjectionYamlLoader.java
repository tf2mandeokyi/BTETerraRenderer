package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.projection.YamlTileProjection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProjectionYamlLoader extends YamlLoader<Map<String, YamlTileProjection>> {

    public static final ProjectionYamlLoader INSTANCE = new ProjectionYamlLoader(
            "projections", "assets/" + BTETerraRenderer.MODID + "/default_projections.yml"
    );

    public ProjectionYamlLoader(String folderName, String defaultYamlPath) {
        super(folderName, defaultYamlPath);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, YamlTileProjection> load(String fileName, Map<String, Object> data) {

        if(data == null) return Collections.emptyMap();
        Map<String, Object> projections = (Map<String, Object>) data.get("tile_projections");
        Map<String, YamlTileProjection> result = new HashMap<>();

        for(Map.Entry<String, Object> entry : projections.entrySet()) {
            String projectionName = entry.getKey();
            Map<String, Object> projectionObject = (Map<String, Object>) entry.getValue();
            result.put(projectionName, new YamlTileProjection(projectionObject));
        }
        return result;
    }

    @Override
    protected void addToResult(Map<String, YamlTileProjection> originalT, Map<String, YamlTileProjection> newT) {
        originalT.putAll(newT);
    }
}
