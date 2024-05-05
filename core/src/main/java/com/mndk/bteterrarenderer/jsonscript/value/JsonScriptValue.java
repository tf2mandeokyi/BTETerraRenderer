package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScript;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;
import com.mndk.bteterrarenderer.jsonscript.parameter.JsonParameters;

public interface JsonScriptValue {

    JsonScriptValueType getType();

    default ResultTransformer.Val transformer() {
        return ResultTransformer.of(this);
    }

    static JsonScriptValue emptyArray() {
        return json(JsonScript.jsonMapper().createArrayNode());
    }

    static JsonScriptJsonValue json(JsonNode node) {
        return new JsonScriptJsonValue(node);
    }

    static JsonScriptJsonValue jsonNull() {
        return JsonScriptJsonValue.NULL;
    }

    static JsonScriptFunctionValue function(String name, JsonParameters parameters, JsonExpression expression,
                                            JsonScriptScope definedAt)
    {
        return new JsonScriptFunctionValue(name, parameters, expression, definedAt);
    }
}
