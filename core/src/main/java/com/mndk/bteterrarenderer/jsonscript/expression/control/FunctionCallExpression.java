package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ArrayArgumentAcceptable;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptFunctionValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class FunctionCallExpression extends JsonExpression {

    private final String name;
    private final JsonNode argument;
    private final ExpressionCallerInfo info;

    @JsonCreator
    @ArrayArgumentAcceptable
    public FunctionCallExpression(@JsonProperty(value = "name", required = true) String name,
                                  @JsonProperty(value = "argument", required = true) JsonNode argument) {
        this.name = name;
        this.argument = argument;
        this.info = new ExpressionCallerInfo(this, name);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        JsonScriptValue value = runtime.getCurrentScope().getVariableValue(this.name);
        if(value == null) {
            return ExpressionResult.error("function " + this.name + " not defined", this.info);
        }

        if(!(value instanceof JsonScriptFunctionValue)) {
            return ExpressionResult.error("variable should be a function type", this.info);
        }

        JsonScriptFunctionValue function = (JsonScriptFunctionValue) value;
        return function.run(runtime, this.argument, this.info);
    }
}
