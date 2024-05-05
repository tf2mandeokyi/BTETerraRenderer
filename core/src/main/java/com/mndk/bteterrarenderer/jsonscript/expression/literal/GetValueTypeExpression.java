package com.mndk.bteterrarenderer.jsonscript.expression.literal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

import javax.annotation.Nonnull;

@JsonDeserialize
public class GetValueTypeExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(GetValueTypeExpression.class);

    private final JsonExpression value;

    @JsonCreator
    public GetValueTypeExpression(JsonExpression value) {
        this.value = value;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        ExpressionResult result = this.value.run(runtime, INFO);
        if(result.isBreakType()) return result;
        return ExpressionResult.ok(new TextNode(result.getValue().getType().toString()));
    }
}
