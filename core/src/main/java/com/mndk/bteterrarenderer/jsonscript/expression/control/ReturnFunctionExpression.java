package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ArrayArgumentAcceptable;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonDeserialize
public class ReturnFunctionExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(ReturnFunctionExpression.class);

    @Nullable
    private final JsonExpression value;

    @JsonCreator
    @ArrayArgumentAcceptable
    public ReturnFunctionExpression(@Nullable @JsonProperty(value = "value") JsonExpression value) {
        this.value = value;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        if(this.value == null) {
            return ExpressionResult.returnExpression();
        }
        ExpressionResult result = this.value.run(runtime, INFO);
        if(result.isError()) {
            return result;
        }
        else if(result.isBreakType()) {
            return ExpressionResult.error("cannot break/continue/return inside a return expression", INFO);
        }
        return ExpressionResult.returnExpression(result.getValue());
    }
}
