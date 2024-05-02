package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;
import com.mndk.bteterrarenderer.jsonscript.exp.func.JsonParameters;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class LambdaExpression implements JsonExpression {

    private final JsonParameters parameters;
    private final JsonExpression expression;

    @JsonCreator
    @JsonExpressionCreator
    public LambdaExpression(@JsonProperty(value = "parameters", required = true) JsonParameters parameters,
                            @JsonProperty(value = "expression", required = true) JsonExpression expression) {
        this.parameters = parameters;
        this.expression = expression;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        JsonScriptValue function = JsonScriptValue.function("<lambda>",
                this.parameters, this.expression, runtime.getCurrentScope());
        return ExpressionResult.ok(function);
    }
}
