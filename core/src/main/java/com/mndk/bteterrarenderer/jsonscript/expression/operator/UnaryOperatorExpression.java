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
public class UnaryOperatorExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(UnaryOperatorExpression.class);

    private final JsonExpression expression;
    private final JsonUnaryOperator operator;

    @JsonCreator
    @JsonExpressionCreator
    public UnaryOperatorExpression(@JsonProperty(value = "operator", required = true) JsonUnaryOperator operator,
                                   @JsonProperty(value = "value", required = true) JsonExpression expression) {
        this.expression = expression;
        this.operator = operator;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return this.operator.run(runtime, INFO, this.expression);
    }
}
