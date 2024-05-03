package com.mndk.bteterrarenderer.jsonscript;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonScript {

    public JsonMapper jsonMapper() {
        return BTETerraRendererConstants.JSON_MAPPER;
    }

}
