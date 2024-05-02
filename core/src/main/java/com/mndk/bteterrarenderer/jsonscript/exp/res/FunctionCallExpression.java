package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptFunctionValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class FunctionCallExpression implements JsonExpression {

    private final String name;
    private final JsonNode argument;

    @JsonCreator
    @JsonExpressionCreator
    public FunctionCallExpression(@JsonProperty(value = "name", required = true) String name,
                                  @JsonProperty(value = "argument", required = true) JsonNode argument) {
        this.name = name;
        this.argument = argument;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        JsonScriptValue value = runtime.getCurrentScope().getVariableValue(this.name)
                .orElseThrow(() -> runtime.exception("function " + this.name + " not defined"));

        JsonScriptFunctionValue function = value.getAsFunction();
        return function.run(runtime, this.argument);
    }
}
