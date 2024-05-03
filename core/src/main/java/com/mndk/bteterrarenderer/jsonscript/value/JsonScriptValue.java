package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.exp.func.JsonParameters;

public interface JsonScriptValue {

    static JsonScriptValue emptyArray() {
        return json(JsonScript.jsonMapper().createArrayNode());
    }

    static JsonScriptValue json(JsonNode node) {
        return new JsonScriptJsonValue(node);
    }

    static JsonScriptValue jsonNull() {
        return JsonScriptJsonValue.NULL;
    }

    static JsonScriptValue function(String name, JsonParameters parameters, JsonExpression expression,
                                    JsonScriptScope definedAt)
    {
        return new JsonScriptFunctionValue(name, parameters, expression, definedAt);
    }
}
