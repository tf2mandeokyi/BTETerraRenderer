package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Ogc3dTiles {

    public JsonMapper jsonMapper() {
        return BTETerraRenderer.JSON_MAPPER;
    }

}
