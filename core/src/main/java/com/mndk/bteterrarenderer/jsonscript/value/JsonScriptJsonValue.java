package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import lombok.Data;

@Data
public class JsonScriptJsonValue implements JsonScriptValue {
    public static final JsonScriptJsonValue NULL = new JsonScriptJsonValue(NullNode.getInstance());

    private final JsonNode value;

    @Override
    public Type getType() {
        return Type.JSON;
    }

    @Override
    public JsonNode getAsJsonValue() {
        return this.value;
    }

    @Override
    public JsonScriptFunctionValue getAsFunction() throws ExpressionRunException {
        throw new ExpressionRunException("cannot convert json to function");
    }

    public String toString() {
        return this.value.toString();
    }
}
