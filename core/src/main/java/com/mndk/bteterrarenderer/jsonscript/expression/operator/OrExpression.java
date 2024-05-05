package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ArrayArgumentAcceptable;

import javax.annotation.Nonnull;

@JsonDeserialize
public class OrExpression extends JsonExpression {

    private final JsonExpression left, right;

    @JsonCreator
    @ArrayArgumentAcceptable
    public OrExpression(@JsonProperty(value = "left", required = true) JsonExpression left,
                        @JsonProperty(value = "right", required = true) JsonExpression right) {
        this.left = left;
        this.right = right;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return JsonBinaryOperator.LOGICAL_OR.run(runtime, this.left, this.right);
    }
}
