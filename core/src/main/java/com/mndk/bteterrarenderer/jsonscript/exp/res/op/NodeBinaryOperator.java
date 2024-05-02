package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

@FunctionalInterface
interface NodeBinaryOperator extends ExpressionBinaryOperator {
    JsonNode run(JsonScriptRuntime runtime, JsonNode left, JsonNode right) throws ExpressionRunException;

    @Override
    default ExpressionResult run(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right)
            throws ExpressionRunException
    {
        ExpressionResult result;

        // LEFT
        if((result = left.run(runtime)).isBreakType()) return result;
        JsonNode leftValue = JsonParserUtil.toBiggerPrimitiveNode(result.getValue().getAsJsonValue());

        // RIGHT
        if((result = right.run(runtime)).isBreakType()) return result;
        JsonNode rightValue = JsonParserUtil.toBiggerPrimitiveNode(result.getValue().getAsJsonValue());

        return ExpressionResult.ok(this.run(runtime, leftValue, rightValue));
    }
}
