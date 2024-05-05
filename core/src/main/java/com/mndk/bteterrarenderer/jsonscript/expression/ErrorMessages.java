package com.mndk.bteterrarenderer.jsonscript.expression;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValueType;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class ErrorMessages {

    public Function<JsonScriptValueType, String> valueMustBeJson(String variableName) {
        return type -> variableName + " must be a json type, instead was " + type;
    }

    public Function<JsonNodeType, String> nodeMustBeBoolean(String variableName) {
        return type -> variableName + " must be a boolean type, instead was " + type;
    }

    public Function<JsonNodeType, String> nodeMustBeTextual(String variableName) {
        return type -> variableName + " must be textual, instead its type was " + type;
    }

}
