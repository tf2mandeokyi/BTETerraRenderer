package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Ogc3dTiles {

    public JsonMapper jsonMapper() {
        return BTETerraRendererConstants.JSON_MAPPER;
    }

}
