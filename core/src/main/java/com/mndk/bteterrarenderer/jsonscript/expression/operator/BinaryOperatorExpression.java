package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpressionCreator;

import javax.annotation.Nonnull;

@JsonDeserialize
public class BinaryOperatorExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(BinaryOperatorExpression.class);

    private final JsonExpression left, right;
    private final JsonBinaryOperator operator;

    @JsonCreator
    @JsonExpressionCreator
    public BinaryOperatorExpression(@JsonProperty(value = "left", required = true) JsonExpression left,
                                    @JsonProperty(value = "operator", required = true) JsonBinaryOperator operator,
                                    @JsonProperty(value = "right", required = true) JsonExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return this.operator.run(runtime, INFO, this.left, this.right);
    }
}
