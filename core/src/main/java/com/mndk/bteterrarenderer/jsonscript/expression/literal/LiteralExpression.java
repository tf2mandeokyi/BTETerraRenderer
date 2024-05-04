package com.mndk.bteterrarenderer.jsonscript.expression.literal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

import javax.annotation.Nonnull;

@JsonDeserialize
public class LiteralExpression extends JsonExpression {

    private final JsonNode literal;

    @JsonCreator
    public LiteralExpression(JsonNode literal) {
        this.literal = JsonParserUtil.toBiggerPrimitiveNode(literal);
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return ExpressionResult.ok(this.literal);
    }
}
