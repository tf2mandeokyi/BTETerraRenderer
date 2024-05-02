package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.*;

import javax.annotation.Nonnull;

@JsonDeserialize
public class BinaryOperationExpression implements JsonExpression {

    private final JsonExpression left;
    private final JsonBinaryOperator operator;
    private final JsonExpression right;

    @JsonCreator
    @JsonExpressionCreator
    public BinaryOperationExpression(@JsonProperty(value = "left", required = true) JsonExpression left,
                                     @JsonProperty(value = "operator", required = true) JsonBinaryOperator operator,
                                     @JsonProperty(value = "right", required = true) JsonExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        return this.operator.run(runtime, this.left, this.right);
    }
}
