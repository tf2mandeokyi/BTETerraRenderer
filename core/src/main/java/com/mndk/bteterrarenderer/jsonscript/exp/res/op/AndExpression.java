package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpressionCreator;

import javax.annotation.Nonnull;

@JsonDeserialize
public class AndExpression extends JsonExpression {

    private final JsonExpression left, right;

    @JsonCreator
    @JsonExpressionCreator
    public AndExpression(@JsonProperty(value = "left", required = true) JsonExpression left,
                         @JsonProperty(value = "right", required = true) JsonExpression right) {
        this.left = left;
        this.right = right;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return JsonBinaryOperator.LOGICAL_AND.run(runtime, this.left, this.right);
    }
}