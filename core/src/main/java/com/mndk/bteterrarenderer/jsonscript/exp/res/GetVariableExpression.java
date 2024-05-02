package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class GetVariableExpression implements JsonExpression {

    private final String name;

    @JsonCreator
    public GetVariableExpression(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        JsonScriptValue value = runtime.getCurrentScope().getVariableValue(this.name)
                .orElseThrow(() -> runtime.exception("variable " + this.name + " not defined"));
        return ExpressionResult.ok(value);
    }
}
