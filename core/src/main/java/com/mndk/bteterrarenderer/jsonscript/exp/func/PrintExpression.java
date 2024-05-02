package com.mndk.bteterrarenderer.jsonscript.exp.func;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;

@JsonDeserialize
public class PrintExpression implements JsonExpression {

    private final JsonExpression expression;

    @JsonCreator
    public PrintExpression(JsonExpression expression) {
        this.expression = expression;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        ExpressionResult result = this.expression.run(runtime);
        if(result.isBreakType()) return result;

        JsonScriptValue value = result.getValue();
        runtime.printValue(value);
        return ExpressionResult.ok();
    }
}
