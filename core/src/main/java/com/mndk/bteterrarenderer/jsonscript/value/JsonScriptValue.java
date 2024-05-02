package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.jsonscript.Scope;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.exp.func.JsonParameters;

public interface JsonScriptValue {

    Type getType();
    JsonNode getAsJsonValue() throws ExpressionRunException;
    JsonScriptFunctionValue getAsFunction() throws ExpressionRunException;

    static JsonScriptValue emptyArray() {
        return json(BTETerraRendererConstants.JSON_MAPPER.createArrayNode());
    }

    static JsonScriptValue json(JsonNode node) {
        return new JsonScriptJsonValue(node);
    }

    static JsonScriptValue jsonNull() {
        return JsonScriptJsonValue.NULL;
    }

    static JsonScriptValue function(String name, JsonParameters parameters, JsonExpression expression, Scope definedAt) {
        return new JsonScriptFunctionValue(name, parameters, expression, definedAt);
    }

    enum Type {
        JSON, FUNCTION
    }
}
